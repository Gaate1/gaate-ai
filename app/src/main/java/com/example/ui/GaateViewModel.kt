package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object PinLogin : Screen()
    object MainDashboard : Screen()
}

sealed class SubView {
    object Home : SubView()
    object Wallet : SubView()
    object Payments : SubView()
    object Marketplace : SubView()
    object GaateAI : SubView()
    object Messenger : SubView()
    object TaxiBooking : SubView()
    object MiniApps : SubView()
    object Business : SubView()
    object Profile : SubView()
}

// Product in African Marketplace
data class MarketProduct(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val rating: Float,
    val category: String,
    val seller: String
)

class GaateViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "gaateone_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = AppRepository(db)

    // --- Core Navigation States ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentSubView = MutableStateFlow<SubView>(SubView.Home)
    val currentSubView: StateFlow<SubView> = _currentSubView.asStateFlow()

    // --- Authentication & Security State ---
    val userProfile = repository.userProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val projectConfig = repository.projectConfig.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val pinInput = MutableStateFlow("")
    val isPinError = MutableStateFlow(false)
    val showPinVerifyDialog = MutableStateFlow(false)

    // --- Financial States ---
    val transactions = repository.transactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Balances in local memory, synchronized with transactions
    private val _mainBalance = MutableStateFlow(125000.0) // XOF
    val mainBalance: StateFlow<Double> = _mainBalance.asStateFlow()

    private val _currenciesList = listOf("XOF", "XAF", "NGN", "KES", "USD", "EUR")
    val currenciesList: List<String> = _currenciesList

    private val _selectedCurrency = MutableStateFlow("XOF")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    // --- Marketplace state ---
    val productsList = listOf(
        MarketProduct(1, "Tissu Wax Sénégalais Premium", "Tissu traditionnel multicolore 100% coton de qualité supérieure, idéal pour boubous et robes.", 15000.0, "", 4.9f, "Mode", "Siga Wax Dakar"),
        MarketProduct(2, "Café Touba Épicé (500g)", "Véritable café de Touba torréfié au poivre de Selim (Jar). Arôme puissant et énergisant.", 2500.0, "", 4.8f, "Alimentation", "Touba Épices"),
        MarketProduct(3, "Statue d'Art en Ébène", "Statue sculptée entièrement à la main par des artisans d'art en bois d'ébène massif.", 45000.0, "", 5.0f, "Déco", "Artisans Soumbédioune"),
        MarketProduct(4, "Panier Tressé de Dakar", "Panier décoratif et utilitaire tressé à la main en paille sauvage locale.", 7500.0, "", 4.6f, "Déco", "Paniers du Baol"),
        MarketProduct(5, "Miel Pur de Casamance", "Miel sauvage 100% naturel récolté dans les forêts denses de la Casamance.", 6000.0, "", 4.7f, "Alimentation", "Saveurs Casamance")
    )

    val cartItems = repository.cartItems.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _marketplaceSearchQuery = MutableStateFlow("")
    val marketplaceSearchQuery: StateFlow<String> = _marketplaceSearchQuery.asStateFlow()

    // --- Messenger State ---
    private val _activeChatChannel = MutableStateFlow("support") // support, dramane, fatou
    val activeChatChannel: StateFlow<String> = _activeChatChannel.asStateFlow()

    val chatMessages = _activeChatChannel.flatMapLatest { channel ->
        repository.getChatMessages(channel)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messageInput = MutableStateFlow("")

    // --- Taxi Booking State ---
    enum class TaxiStage { IDLE, BOOKING, MATCHING, DRIVING, ARRIVED }
    private val _taxiStage = MutableStateFlow(TaxiStage.IDLE)
    val taxiStage: StateFlow<TaxiStage> = _taxiStage.asStateFlow()

    private val _taxiCarOffset = MutableStateFlow(0f) // For custom GPS animation progress
    val taxiCarOffset: StateFlow<Float> = _taxiCarOffset.asStateFlow()

    val taxiDestination = MutableStateFlow("")
    val selectedRideType = MutableStateFlow("Auto Classic") // Moto-Taxi, Auto Classic, Premium Black

    // --- Gaate AI Chat Assistant ---
    private val _aiMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("Bonjour ! Je suis Gaate AI, votre conseiller Super App. Posez-moi des questions sur vos transactions, demandez-moi d'analyser vos dépenses, ou de vous traduire un message." to false)
    )
    val aiMessages: StateFlow<List<Pair<String, Boolean>>> = _aiMessages.asStateFlow()

    val aiInput = MutableStateFlow("")
    val aiLoading = MutableStateFlow(false)

    // --- Business Console State ---
    private val _businessInvoices = MutableStateFlow<List<Pair<String, Double>>>(
        listOf("Facture #098 - Boutik Wax" to 30000.0, "Facture #099 - Epicerie Touba" to 12500.0)
    )
    val businessInvoices: StateFlow<List<Pair<String, Double>>> = _businessInvoices.asStateFlow()
    val invoiceClient = MutableStateFlow("")
    val invoiceAmount = MutableStateFlow("")

    // --- Notification logs ---
    private val _notifications = MutableStateFlow<List<String>>(
        listOf("Bienvenue sur Gaate One ! Votre portefeuille est prêt.", "Alerte de sécurité : Double Authentification activée.")
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            // Recalculate balance based on database transactions initially
            recalculateBalanceFromTransactions()
        }

        // Start splash delay
        viewModelScope.launch {
            delay(3500)
            _currentScreen.value = Screen.PinLogin
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSubView(subView: SubView) {
        _currentSubView.value = subView
    }

    // --- Auth Logic ---
    fun appendPin(digit: String) {
        if (pinInput.value.length < 6) {
            pinInput.value += digit
            if (pinInput.value.length == 6) {
                verifyPinAndLogin()
            }
        }
    }

    fun deletePinDigit() {
        if (pinInput.value.isNotEmpty()) {
            pinInput.value = pinInput.value.dropLast(1)
        }
    }

    private fun verifyPinAndLogin() {
        viewModelScope.launch {
            val profile = userProfile.first { it != null }
            if (pinInput.value == (profile?.pin ?: "123456")) {
                isPinError.value = false
                pinInput.value = ""
                _currentScreen.value = Screen.MainDashboard
            } else {
                isPinError.value = true
                pinInput.value = ""
                delay(1000)
                isPinError.value = false
            }
        }
    }

    // --- Core Wallet Operations ---
    fun selectCurrency(currency: String) {
        val previous = _selectedCurrency.value
        if (previous != currency) {
            _selectedCurrency.value = currency
            // Simulate conversion of balance just for UI feel
            val rate = when {
                previous == "XOF" && currency == "USD" -> 0.0016
                previous == "USD" && currency == "XOF" -> 600.0
                previous == "XOF" && currency == "NGN" -> 2.45
                previous == "NGN" && currency == "XOF" -> 0.4
                else -> 1.0
            }
            _mainBalance.value = _mainBalance.value * rate
        }
    }

    fun doRecharge(amount: Double, operator: String) {
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                currency = _selectedCurrency.value,
                type = "RECHARGE",
                category = operator,
                title = "Recharge via $operator"
            )
            recalculateBalanceFromTransactions()
            pushNotification("Recharge réussie de $amount ${_selectedCurrency.value} via $operator.")
        }
    }

    fun doSendMoney(amount: Double, recipient: String, operator: String) {
        if (_mainBalance.value >= amount) {
            viewModelScope.launch {
                repository.addTransaction(
                    amount = amount,
                    currency = _selectedCurrency.value,
                    type = "SEND",
                    category = operator,
                    title = "Transfert à $recipient ($operator)"
                )
                recalculateBalanceFromTransactions()
                pushNotification("Envoi de $amount ${_selectedCurrency.value} à $recipient effectué.")
            }
        }
    }

    fun payBill(amount: Double, billName: String, accountNumber: String) {
        if (_mainBalance.value >= amount) {
            viewModelScope.launch {
                repository.addTransaction(
                    amount = amount,
                    currency = _selectedCurrency.value,
                    type = "BILL",
                    category = billName,
                    title = "Facture $billName #$accountNumber"
                )
                recalculateBalanceFromTransactions()
                pushNotification("Facture $billName de $amount ${_selectedCurrency.value} payée avec succès.")
            }
        }
    }

    private suspend fun recalculateBalanceFromTransactions() {
        val txList = transactions.first()
        var balance = 150000.0 // Starting base balance XOF
        txList.forEach { tx ->
            when (tx.type) {
                "RECEIVE", "RECHARGE" -> balance += tx.amount
                "SEND", "BILL", "WITHDRAW" -> balance -= tx.amount
            }
        }
        _mainBalance.value = balance
    }

    // --- Marketplace Logic ---
    fun updateMarketplaceSearch(query: String) {
        _marketplaceSearchQuery.value = query
    }

    fun addToCart(product: MarketProduct) {
        viewModelScope.launch {
            repository.addToCart(product.id, product.title, product.price)
        }
    }

    fun removeFromCart(id: Int) {
        viewModelScope.launch {
            repository.removeFromCart(id)
        }
    }

    fun checkoutCart() {
        viewModelScope.launch {
            val items = cartItems.first()
            if (items.isNotEmpty()) {
                val total = items.sumOf { it.price * it.quantity }
                if (_mainBalance.value >= total) {
                    repository.addTransaction(
                        amount = total,
                        currency = _selectedCurrency.value,
                        type = "BILL",
                        category = "Marketplace",
                        title = "Achat Super Marché (${items.size} articles)"
                    )
                    repository.clearCart()
                    recalculateBalanceFromTransactions()
                    pushNotification("Commande Marketplace validée pour $total ${_selectedCurrency.value}. Livraison en cours !")
                }
            }
        }
    }

    // --- Chat Messenger Logic ---
    fun selectChatChannel(channel: String) {
        _activeChatChannel.value = channel
    }

    fun sendChatMessage() {
        val text = messageInput.value.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                val channel = _activeChatChannel.value
                repository.saveMessage(channel, "Moi", text, true)
                messageInput.value = ""

                // Simulate reply
                delay(1200)
                val reply = when (channel) {
                    "support" -> "Merci pour votre message. Notre support Gaate One traite votre demande dans les plus brefs délais."
                    "dramane" -> "Bien reçu ! Je t'envoie les fonds via Wave tout de suite."
                    "fatou" -> "Bonjour, oui l'article Wax de Dakar est disponible. Je peux vous l'envoyer par coursier Gaate !"
                    else -> "Message reçu !"
                }
                repository.saveMessage(channel, if (channel == "support") "Support" else channel.capitalize(), reply, false)
            }
        }
    }

    // --- Taxi Booking Sim ---
    fun startTaxiBooking() {
        _taxiStage.value = TaxiStage.MATCHING
        viewModelScope.launch {
            delay(2500)
            _taxiStage.value = TaxiStage.DRIVING
            _taxiCarOffset.value = 0f
            // Drive animation
            for (i in 1..100) {
                delay(80)
                _taxiCarOffset.value = i / 100f
            }
            _taxiStage.value = TaxiStage.ARRIVED
            pushNotification("Votre chauffeur Gaate Ride est arrivé à destination !")
        }
    }

    fun resetTaxiSim() {
        _taxiStage.value = TaxiStage.IDLE
        _taxiCarOffset.value = 0f
    }

    // --- Gaate AI Conversational Assistant ---
    fun sendAiQuestion() {
        val question = aiInput.value.trim()
        if (question.isNotEmpty()) {
            _aiMessages.value = _aiMessages.value + (question to true)
            aiInput.value = ""
            aiLoading.value = true

            viewModelScope.launch {
                val contextString = "Balance active : ${_mainBalance.value} ${_selectedCurrency.value}. Nombre de transactions récentes : ${transactions.value.size}. KYC : Validé."
                val answer = repository.askGaateAI(question, contextString)
                aiLoading.value = false
                _aiMessages.value = _aiMessages.value + (answer to false)
            }
        }
    }

    // --- Business Console ---
    fun createBusinessInvoice() {
        val client = invoiceClient.value.trim()
        val amtStr = invoiceAmount.value.trim()
        val amt = amtStr.toDoubleOrNull() ?: 0.0
        if (client.isNotEmpty() && amt > 0.0) {
            _businessInvoices.value = _businessInvoices.value + ("Facture #${100 + _businessInvoices.value.size} - $client" to amt)
            invoiceClient.value = ""
            invoiceAmount.value = ""
            pushNotification("Nouvelle facture créée pour $client d'un montant de $amt XOF.")
        }
    }

    // --- Update KYC / Profile ---
    fun updateProfileInfo(nom: String, prenom: String, pays: String, ville: String, numero: String, email: String, langue: String, devise: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveProfile(
                current.copy(
                    nom = nom,
                    prenom = prenom,
                    pays = pays,
                    ville = ville,
                    numero = numero,
                    email = email,
                    langue = langue,
                    devise = devise
                )
            )
            pushNotification("Profil mis à jour avec succès.")
        }
    }

    fun updateProjectConfig(projectName: String, founderName: String, contactNo: String, email: String) {
        viewModelScope.launch {
            val current = projectConfig.value ?: ProjectConfig()
            repository.saveProjectConfig(
                current.copy(
                    projectName = projectName,
                    founderName = founderName,
                    contactNo = contactNo,
                    email = email
                )
            )
            pushNotification("Coordonnées de l'administration mises à jour avec succès.")
        }
    }

    fun simulateKycUpload() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveProfile(current.copy(kycStatus = "PENDING"))
            pushNotification("Documents KYC soumis pour vérification.")
            delay(3000)
            repository.saveProfile(current.copy(kycStatus = "VERIFIED"))
            pushNotification("Félicitations ! Vos documents KYC ont été validés par l'administration Gaate.")
        }
    }

    private fun pushNotification(text: String) {
        _notifications.value = listOf(text) + _notifications.value
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
