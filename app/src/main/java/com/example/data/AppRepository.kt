package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AppRepository(
    private val db: AppDatabase
) {
    val transactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    val cartItems: Flow<List<CartItem>> = db.cartItemDao().getCartItems()
    val userProfile: Flow<UserProfile?> = db.userProfileDao().getUserProfile()
    val projectConfig: Flow<ProjectConfig?> = db.projectConfigDao().getProjectConfig()

    fun getChatMessages(channel: String): Flow<List<ChatMessage>> =
        db.chatMessageDao().getMessagesForChannel(channel)

    suspend fun addTransaction(amount: Double, currency: String, type: String, category: String, title: String) {
        db.transactionDao().insertTransaction(
            Transaction(amount = amount, currency = currency, type = type, category = category, title = title)
        )
    }

    suspend fun clearTransactions() {
        db.transactionDao().clearAll()
    }

    suspend fun saveMessage(channel: String, senderName: String, content: String, isFromMe: Boolean) {
        db.chatMessageDao().insertMessage(
            ChatMessage(senderName = senderName, content = content, isFromMe = isFromMe, channel = channel)
        )
    }

    suspend fun addToCart(id: Int, title: String, price: Double) {
        val currentItems = db.cartItemDao().getCartItems().firstOrNull() ?: emptyList()
        val existing = currentItems.find { it.id == id }
        if (existing != null) {
            db.cartItemDao().insertOrUpdateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            db.cartItemDao().insertOrUpdateCartItem(CartItem(id = id, title = title, price = price, quantity = 1))
        }
    }

    suspend fun removeFromCart(id: Int) {
        db.cartItemDao().removeCartItem(id)
    }

    suspend fun clearCart() {
        db.cartItemDao().clearCart()
    }

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileDao().saveUserProfile(profile)
    }

    suspend fun saveProjectConfig(config: ProjectConfig) {
        db.projectConfigDao().saveProjectConfig(config)
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val currentProfile = db.userProfileDao().getUserProfile().firstOrNull()
        if (currentProfile == null) {
            db.userProfileDao().saveUserProfile(UserProfile())
        }

        val currentConfig = db.projectConfigDao().getProjectConfig().firstOrNull()
        if (currentConfig == null) {
            db.projectConfigDao().saveProjectConfig(ProjectConfig())
        }

        val txs = db.transactionDao().getAllTransactions().firstOrNull() ?: emptyList()
        if (txs.isEmpty()) {
            addTransaction(25000.0, "XOF", "RECEIVE", "Wave", "Dépôt Wave")
            addTransaction(8500.0, "XOF", "BILL", "Senelec", "Facture Électricité Senelec")
            addTransaction(15000.0, "XOF", "SEND", "Orange Money", "Transfert à Fatou")
            addTransaction(3000.0, "XOF", "BILL", "Marketplace", "Achat Panier Artisanat")
        }

        // Initialize chats for support
        val supportMsgs = db.chatMessageDao().getMessagesForChannel("support").firstOrNull() ?: emptyList()
        if (supportMsgs.isEmpty()) {
            db.chatMessageDao().insertMessage(
                ChatMessage(senderName = "Gaate Support", content = "Bonjour ! Bienvenue chez Gaate One. Comment pouvons-nous vous aider aujourd'hui ?", isFromMe = false, channel = "support")
            )
        }
    }

    suspend fun askGaateAI(prompt: String, contextString: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val cleanKey = apiKey.trim()

        // If no real API key is provided, return rich offline / mocked answers
        if (cleanKey.isEmpty() || cleanKey == "MY_GEMINI_API_KEY" || cleanKey.startsWith("PLACEHOLDER")) {
            return@withContext getLocalSmartResponse(prompt)
        }

        try {
            val systemPrompt = "Vous êtes Gaate AI, l'assistant intelligent de la Super App africaine 'Gaate One'. Vous aidez les utilisateurs à gérer leur budget, comprendre leurs transactions, traduire, scanner et planifier. Répondez de manière concise, polie et moderne en français. Utilisez des puces ou émoticônes si nécessaire. Voici le contexte actuel de l'utilisateur: $contextString"
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )

            val response = GeminiClient.service.generateContent(cleanKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrEmpty()) {
                return@withContext resultText
            } else {
                return@withContext "Désolé, je n'ai pas pu générer de réponse. Voici une réponse locale : \n\n${getLocalSmartResponse(prompt)}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Note: [Mode Hors-ligne / Erreur de Connexion]\n\n${getLocalSmartResponse(prompt)}"
        }
    }

    private fun getLocalSmartResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("budget") || lower.contains("dépense") || lower.contains("analyse") || lower.contains("argent") -> {
                "📊 **Analyse Budgétaire Gaate AI** :\n\n" +
                        "Ce mois-ci, vos transactions se répartissent ainsi :\n" +
                        "• **Services de Base** (Senelec/SDE) : 34%\n" +
                        "• **Dépôts/Transferts** (Wave/Orange) : 48%\n" +
                        "• **Achats Marketplace** : 18%\n\n" +
                        "💡 *Conseil Intelligent* : Vos dépenses de services publics ont baissé de 8% par rapport au mois dernier. Vous êtes sur la bonne voie ! Pensez à épargner 10% de vos gains dans votre coffre-fort Gaate."
            }
            lower.contains("langue") || lower.contains("wolof") || lower.contains("swahili") || lower.contains("afrique") || lower.contains("parle") -> {
                "🌍 **Support Multilingue Gaate One** :\n\n" +
                        "Je parle couramment :\n" +
                        "• **Langues Africaines** : Wolof, Swahili, Yorùbá, Haoussa, Bambara, Amharique, Lingala, Zulu.\n" +
                        "• **Langues Internationales** : Français, Anglais, Arabe, Portugais.\n\n" +
                        "Dites-moi simplement : *'Parle Wolof'* ou *'Translate to English'* pour commencer !"
            }
            lower.contains("transfert") || lower.contains("envoyer") || lower.contains("mobile money") || lower.contains("wave") || lower.contains("orange") -> {
                "💸 **Aide aux Transferts Gaate One** :\n\n" +
                        "1. Allez sur l'onglet **Paiements** ou cliquez sur **Envoyer**.\n" +
                        "2. Sélectionnez le mode de transfert (Gaate-to-Gaate, Wave, Orange Money, MTN, Moov, ou Virement Bancaire).\n" +
                        "3. Entrez le numéro de téléphone ou l'IBAN du destinataire.\n" +
                        "4. Validez instantanément et de façon sécurisée avec votre PIN à 6 chiffres ou empreinte biométrique."
            }
            lower.contains("sécurité") || lower.contains("hack") || lower.contains("vol") || lower.contains("kyc") -> {
                "🔒 **Sécurité et Conformité Gaate One** :\n\n" +
                        "Toutes vos données et transactions sont protégées avec :\n" +
                        "• Le chiffrement de bout en bout AES-256.\n" +
                        "• La double authentification (2FA).\n" +
                        "• La détection automatique des fraudes par IA.\n" +
                        "• La conformité réglementaire locale et le RGPD."
            }
            else -> {
                "✨ **Gaate AI à votre service !**\n\n" +
                        "En tant qu'assistant de la première Super App d'Afrique, je peux :\n" +
                        "• Calculer vos frais de transaction.\n" +
                        "• Vous aider à épargner ou budgétiser.\n" +
                        "• Traduire des conversations instantanément.\n" +
                        "• Vous guider pour scanner des documents KYC.\n\n" +
                        "Posez-moi une question sur vos finances ou nos services !"
            }
        }
    }
}
