package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer

data class SplashParticle(
    val initialX: Float,
    val initialY: Float,
    val size: Float,
    val speedY: Float,
    val speedX: Float,
    val maxAlpha: Float
)

@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Generate stable list of random offsets
    val particles = remember {
        List(30) {
            SplashParticle(
                initialX = Math.random().toFloat(),
                initialY = Math.random().toFloat(),
                size = (3..8).random().toFloat(),
                speedY = (0.04f..0.12f).random(),
                speedX = (-0.03f..0.03f).random(),
                maxAlpha = (0.25f..0.65f).random()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Update position loop
            val x = (particle.initialX + particle.speedX * time) % 1f
            val y = (particle.initialY - particle.speedY * time + 1f) % 1f
            
            val posX = x * size.width
            val posY = y * size.height
            
            // Pulse alpha animation
            val alpha = particle.maxAlpha * (0.4f + 0.6f * kotlin.math.sin((time * 4.0f + particle.initialX * 12.0f).toDouble()).toFloat())

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = alpha),
                        VioletPremium.copy(alpha = alpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(posX, posY),
                    radius = particle.size * 3.5f
                ),
                radius = particle.size * 3.5f,
                center = Offset(posX, posY)
            )
        }
    }
}

private fun ClosedRange<Float>.random() = 
    (Math.random() * (endInclusive - start) + start).toFloat()

// --- Core Styled Bird Logo Component (Refactored to Premium 3D Minimalist Hawk) ---
@Composable
fun BirdLogo(
    modifier: Modifier = Modifier,
    tint: Color = VioletLight, // Fallback accent tint
    size: Float = 60f,
    wingExpansion: Float = 1.0f,
    showGlow: Boolean = true
) {
    val finalExpansion = wingExpansion.coerceIn(0f, 1f)
    
    // Linear interpolation helper
    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.dp.toPx()
            val height = size.dp.toPx()
            
            // Ambient subtle futuristic glow background
            if (showGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.35f),
                            VioletPremium.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(width / 2f, height * 0.45f),
                        radius = width * 0.75f
                    ),
                    radius = width * 0.75f,
                    center = Offset(width / 2f, height * 0.45f)
                )
            }

            // Norm maps 0..1 layout bounds to coordinates in canvas pixels
            fun normX(value: Float): Float = value * width
            fun normY(value: Float): Float = value * height

            // 1. HEAD & BEAK Facets (Low-poly 3D aesthetics)
            val headLeftPath = Path().apply {
                moveTo(normX(0.5f), normY(0.15f))
                lineTo(normX(0.44f), normY(0.24f))
                lineTo(normX(0.5f), normY(0.30f))
                close()
            }
            val headRightPath = Path().apply {
                moveTo(normX(0.5f), normY(0.15f))
                lineTo(normX(0.56f), normY(0.24f))
                lineTo(normX(0.5f), normY(0.30f))
                close()
            }

            // 2. BODY / CHEST Facets (3D Diamond Fold)
            val chestLeftPath = Path().apply {
                moveTo(normX(0.5f), normY(0.30f))
                lineTo(normX(0.40f), normY(0.48f))
                lineTo(normX(0.5f), normY(0.85f))
                close()
            }
            val chestRightPath = Path().apply {
                moveTo(normX(0.5f), normY(0.30f))
                lineTo(normX(0.60f), normY(0.48f))
                lineTo(normX(0.5f), normY(0.85f))
                close()
            }

            // 3. LEFT WING Facets (Dynamic deployment coordinates)
            val leftJoinX = 0.43f
            val leftJoinY = 0.38f

            val leftElbowX = lerp(0.33f, 0.18f, finalExpansion)
            val leftElbowY = lerp(0.44f, 0.20f, finalExpansion)

            val leftTipX = lerp(0.26f, 0.02f, finalExpansion)
            val leftTipY = lerp(0.56f, 0.06f, finalExpansion)

            val leftBottomX = lerp(0.38f, 0.28f, finalExpansion)
            val leftBottomY = lerp(0.66f, 0.54f, finalExpansion)

            val leftWingAPath = Path().apply {
                moveTo(normX(leftJoinX), normY(leftJoinY))
                lineTo(normX(leftElbowX), normY(leftElbowY))
                lineTo(normX(leftTipX), normY(leftTipY))
                close()
            }
            val leftWingBPath = Path().apply {
                moveTo(normX(leftJoinX), normY(leftJoinY))
                lineTo(normX(leftTipX), normY(leftTipY))
                lineTo(normX(leftBottomX), normY(leftBottomY))
                close()
            }

            // 4. RIGHT WING Facets (Mirror of left coordinates)
            val rightJoinX = 0.57f
            val rightJoinY = 0.38f

            val rightElbowX = lerp(0.67f, 0.82f, finalExpansion)
            val rightElbowY = lerp(0.44f, 0.20f, finalExpansion)

            val rightTipX = lerp(0.74f, 0.98f, finalExpansion)
            val rightTipY = lerp(0.56f, 0.06f, finalExpansion)

            val rightBottomX = lerp(0.62f, 0.72f, finalExpansion)
            val rightBottomY = lerp(0.66f, 0.54f, finalExpansion)

            val rightWingAPath = Path().apply {
                moveTo(normX(rightJoinX), normY(rightJoinY))
                lineTo(normX(rightElbowX), normY(rightElbowY))
                lineTo(normX(rightTipX), normY(rightTipY))
                close()
            }
            val rightWingBPath = Path().apply {
                moveTo(normX(rightJoinX), normY(rightJoinY))
                lineTo(normX(rightTipX), normY(rightTipY))
                lineTo(normX(rightBottomX), normY(rightBottomY))
                close()
            }

            // --- Draw Polygons with Premium Shaded Gradients ---
            // Head Left Facet
            drawPath(
                path = headLeftPath,
                brush = Brush.verticalGradient(
                    colors = listOf(VioletDark, VioletPremium)
                )
            )
            // Head Right Facet (Bright)
            drawPath(
                path = headRightPath,
                brush = Brush.verticalGradient(
                    colors = listOf(VioletLight, ElectricBlue)
                )
            )

            // Chest Left Facet
            drawPath(
                path = chestLeftPath,
                brush = Brush.verticalGradient(
                    colors = listOf(VioletDark, VioletPremium)
                )
            )
            // Chest Right Facet
            drawPath(
                path = chestRightPath,
                brush = Brush.verticalGradient(
                    colors = listOf(VioletPremium, ElectricBlue)
                )
            )

            // Left Wing A
            drawPath(
                path = leftWingAPath,
                brush = Brush.linearGradient(
                    colors = listOf(VioletDark, VioletPremium)
                )
            )
            // Left Wing B
            drawPath(
                path = leftWingBPath,
                brush = Brush.linearGradient(
                    colors = listOf(VioletPremium, ElectricBlue)
                )
            )

            // Right Wing A
            drawPath(
                path = rightWingAPath,
                brush = Brush.linearGradient(
                    colors = listOf(VioletLight, ElectricBlue)
                )
            )
            // Right Wing B
            drawPath(
                path = rightWingBPath,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBlue, Color.White)
                )
            )
        }
    }
}

// --- SplashScreen ---
@Composable
fun SplashScreen() {
    var startWingDeploy by remember { mutableStateOf(false) }
    var startTextReveal by remember { mutableStateOf(false) }

    val wingExpansion by animateFloatAsState(
        targetValue = if (startWingDeploy) 1f else 0f,
        animationSpec = tween(2200, easing = FastOutSlowInEasing),
        label = "wing_expansion"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (startWingDeploy) 1.1f else 0.6f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startWingDeploy) 1f else 0f,
        animationSpec = tween(1200, easing = LinearEasing),
        label = "logo_alpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startTextReveal) 1f else 0f,
        animationSpec = tween(1500, easing = EaseOutQuad),
        label = "text_alpha"
    )

    val textTranslationY by animateFloatAsState(
        targetValue = if (startTextReveal) 0f else 40f,
        animationSpec = tween(1500, easing = EaseOutQuad),
        label = "text_translation"
    )

    LaunchedEffect(Unit) {
        delay(200)
        startWingDeploy = true
        delay(1200)
        startTextReveal = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Deep premium black background requested
        contentAlignment = Alignment.Center
    ) {
        // Futuristic Glowing Particles
        ParticleBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animate Logo Container
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
            ) {
                BirdLogo(
                    size = 110f,
                    wingExpansion = wingExpansion,
                    showGlow = true
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Reveal Text & Slogan Group
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = textAlpha
                        translationY = textTranslationY
                    }
            ) {
                Text(
                    text = "GAATE ONE",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Premium horizontal line accent
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(VioletPremium, ElectricBlue)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "LA SUPER APP AFRICAINE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = ElectricBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 5.sp
                    )
                )
            }
        }
        
        // Custom production tag
        Text(
            text = "GAATE DEV AI • PRODUCTION READY",
            color = TextGray.copy(alpha = textAlpha * 0.7f),
            fontSize = 11.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .graphicsLayer {
                    translationY = textTranslationY * 0.5f
                },
            letterSpacing = 2.sp
        )
    }
}

// --- PinLoginScreen ---
@Composable
fun PinLoginScreen(viewModel: GaateViewModel) {
    val pin by viewModel.pinInput.collectAsState()
    val isError by viewModel.isPinError.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(SpaceObsidian, SpaceBlack),
                    radius = 1200f
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                BirdLogo(size = 50f, tint = VioletPremium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bienvenue, ${profile?.prenom ?: "Amadou"}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Saisissez votre code PIN à 6 chiffres",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val active = i < pin.length
                    val color = if (isError) AccentRed else if (active) VioletPremium else Color.White.copy(alpha = 0.2f)
                    val scale by animateFloatAsState(targetValue = if (active) 1.2f else 1.0f)
                    
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .drawBehind {
                                drawCircle(
                                    color = color,
                                    radius = (8 * scale).dp.toPx()
                                )
                            }
                    )
                }
            }

            // Obsidian Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("fingerprint", "0", "delete")
                )
                
                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "fingerprint" -> {
                                        IconButton(
                                            onClick = {
                                                // Trigger fast mock login for developer ease
                                                viewModel.appendPin("123456")
                                            },
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(Color(0x0AFFFFFF), CircleShape)
                                                .testTag("biometric_login")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Empreinte digitale",
                                                tint = VioletLight,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    "delete" -> {
                                        IconButton(
                                            onClick = { viewModel.deletePinDigit() },
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(Color(0x0AFFFFFF), CircleShape)
                                                .testTag("delete_pin_digit")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Effacer",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        Surface(
                                            onClick = { viewModel.appendPin(key) },
                                            shape = CircleShape,
                                            color = Color(0x12FFFFFF),
                                            border = BorderStroke(1.dp, Color(0x0DFFFFFF)),
                                            modifier = Modifier
                                                .size(64.dp)
                                                .testTag("keypad_$key")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = key,
                                                    color = Color.White,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Main App Dashboard Shell ---
@Composable
fun MainDashboardScreen(viewModel: GaateViewModel) {
    val currentSubView by viewModel.currentSubView.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val scope = rememberCoroutineScope()
    var showNotifs by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SpaceObsidian,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val navItems = listOf(
                    Triple(SubView.Home, Icons.Default.Home, "Accueil"),
                    Triple(SubView.Wallet, Icons.Default.AccountBalanceWallet, "Wallet"),
                    Triple(SubView.Payments, Icons.Default.Send, "Paiements"),
                    Triple(SubView.Marketplace, Icons.Default.ShoppingBag, "Boutik"),
                    Triple(SubView.GaateAI, Icons.Default.Psychology, "Gaate AI")
                )
                
                navItems.forEach { (subView, icon, label) ->
                    val selected = currentSubView == subView
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setSubView(subView) },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = VioletLight,
                            indicatorColor = VioletPremium,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        ),
                        modifier = Modifier.testTag("nav_${label.lowercase()}")
                    )
                }
            }
        },
        containerColor = SpaceBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Page content animation
            Crossfade(targetState = currentSubView, label = "ViewTransition") { targetSubView ->
                when (targetSubView) {
                    is SubView.Home -> HomeView(viewModel, onNotificationClick = { showNotifs = true })
                    is SubView.Wallet -> WalletView(viewModel)
                    is SubView.Payments -> PaymentsView(viewModel)
                    is SubView.Marketplace -> MarketplaceView(viewModel)
                    is SubView.GaateAI -> GaateAIView(viewModel)
                    is SubView.Messenger -> MessengerView(viewModel)
                    is SubView.TaxiBooking -> TaxiBookingView(viewModel)
                    is SubView.MiniApps -> MiniAppsView(viewModel)
                    is SubView.Business -> BusinessView(viewModel)
                    is SubView.Profile -> ProfileView(viewModel)
                }
            }
            
            // Notification Slider overlay
            if (showNotifs) {
                AlertDialog(
                    onDismissRequest = { showNotifs = false },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearNotifications() }) {
                            Text("Tout effacer", color = AccentRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNotifs = false }) {
                            Text("Fermer", color = Color.White)
                        }
                    },
                    title = { Text("Notifications Gaate One", color = Color.White, fontWeight = FontWeight.Bold) },
                    containerColor = SpaceObsidian,
                    text = {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            if (notifications.isEmpty()) {
                                item {
                                    Text("Aucune notification pour le moment.", color = TextGray)
                                }
                            } else {
                                items(notifications) { notif ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardObsidian),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = VioletLight,
                                                modifier = Modifier.padding(end = 12.dp)
                                            )
                                            Text(notif, color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

// --- Home / Main Dashboard SubView ---
@Composable
fun HomeView(viewModel: GaateViewModel, onNotificationClick: () -> Unit) {
    val profile by viewModel.userProfile.collectAsState()
    val balance by viewModel.mainBalance.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BirdLogo(size = 36f, tint = VioletPremium)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GAATE ONE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AccentGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KYC Vérifié",
                                color = AccentGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.background(CardObsidian, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setSubView(SubView.Profile) },
                        modifier = Modifier.background(CardObsidian, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Beautiful Virtual Card Showcase (Glassmorphic)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { viewModel.setSubView(SubView.Wallet) },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CardObsidianStroke),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VioletPremium, VioletDark, SpaceObsidian)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SOLDE PRINCIPAL", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, letterSpacing = 2.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("%,.0f %s", balance, currency),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            BirdLogo(size = 36f, tint = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "${profile?.prenom ?: "Amadou"} ${profile?.nom ?: "Diallo"}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("•••• •••• •••• 9812", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            // Simulated Visa/Mastercard badge
                            Text(
                                text = "VISA",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }

        // Africa Super Grid Action Cards
        item {
            Text("SERVICES DU QUOTIDIEN", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val gridItems = listOf(
                    listOf(
                        Triple(SubView.Payments, Icons.Default.AccountBalanceWallet, "Mobile Money"),
                        Triple(SubView.TaxiBooking, Icons.Default.LocalTaxi, "Taxi Ride"),
                        Triple(SubView.Marketplace, Icons.Default.ShoppingBag, "Marketplace")
                    ),
                    listOf(
                        Triple(SubView.GaateAI, Icons.Default.Psychology, "Gaate AI"),
                        Triple(SubView.Messenger, Icons.Default.Chat, "Messenger"),
                        Triple(SubView.Business, Icons.Default.BusinessCenter, "Pro / Business")
                    ),
                    listOf(
                        Triple(SubView.MiniApps, Icons.Default.Apps, "Mini Apps"),
                        Triple(SubView.Profile, Icons.Default.VerifiedUser, "Vérif KYC"),
                        Triple(SubView.Wallet, Icons.Default.CreditCard, "Cartes")
                    )
                )

                gridItems.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { (subView, icon, label) ->
                            Card(
                                onClick = { viewModel.setSubView(subView) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(95.dp),
                                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CardObsidianStroke)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = VioletLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transaction History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HISTORIQUE DES TRANSACTIONS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                TextButton(onClick = { viewModel.setSubView(SubView.Wallet) }) {
                    Text("Voir tout", color = VioletLight, fontSize = 12.sp)
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardObsidian),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Aucune transaction enregistrée.", color = TextGray)
                    }
                }
            }
        } else {
            items(transactions.take(5)) { tx ->
                val isPositive = tx.type == "RECEIVE" || tx.type == "RECHARGE"
                val tintColor = if (isPositive) AccentGreen else AccentRed
                val prefix = if (isPositive) "+" else "-"
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardObsidian),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardObsidianStroke.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(VioletPremium.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val txIcon = when (tx.type) {
                                    "BILL" -> Icons.Default.ReceiptLong
                                    "SEND" -> Icons.Default.ArrowUpward
                                    "RECHARGE" -> Icons.Default.Add
                                    else -> Icons.Default.ArrowDownward
                                }
                                Icon(
                                    imageVector = txIcon,
                                    contentDescription = null,
                                    tint = VioletLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(tx.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${tx.category} • Transaction instantanée", color = TextGray, fontSize = 11.sp)
                            }
                        }
                        
                        Text(
                            text = String.format("%s%,.0f %s", prefix, tx.amount, tx.currency),
                            color = tintColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// --- Wallet View SubPage ---
@Composable
fun WalletView(viewModel: GaateViewModel) {
    val balance by viewModel.mainBalance.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currencies = viewModel.currenciesList
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("MON PORTEFEUILLE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Gérez vos comptes multi-devises et cartes", color = TextGray, fontSize = 13.sp)
        }

        // Multi-currency accounts slider simulation
        item {
            Text("COMPTES ACTIFS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                currencies.forEach { curr ->
                    val isSelected = curr == currency
                    Card(
                        onClick = { viewModel.selectCurrency(curr) },
                        modifier = Modifier
                            .width(140.dp)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) VioletPremium else CardObsidian
                        ),
                        border = BorderStroke(1.dp, if (isSelected) VioletLight else CardObsidianStroke)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(curr, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Column {
                                val simulatedBal = if (isSelected) balance else when(curr) {
                                    "USD" -> balance * 0.0016
                                    "EUR" -> balance * 0.0015
                                    "NGN" -> balance * 2.45
                                    else -> balance * 1.0
                                }
                                Text(
                                    text = String.format("%,.0f", simulatedBal),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Solde disponible", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Instant currency converter
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardObsidianStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Conversion Instantanée", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Convertir depuis XOF", color = TextGray, fontSize = 11.sp)
                            Text("10,000 XOF", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = VioletLight, modifier = Modifier.size(28.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Vers USD (Estimation)", color = TextGray, fontSize = 11.sp)
                            Text("16.3 USD", color = AccentGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.selectCurrency("USD") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium)
                    ) {
                        Text("Échanger les Devises", color = Color.White)
                    }
                }
            }
        }

        // Full historic Ledger list
        item {
            Text("HISTORIQUE COMPLET", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (transactions.isEmpty()) {
            item {
                Text("Aucun mouvement enregistré.", color = TextGray)
            }
        } else {
            items(transactions) { tx ->
                val isPositive = tx.type == "RECEIVE" || tx.type == "RECHARGE"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardObsidian),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${tx.type} • ${tx.category}", color = TextGray, fontSize = 11.sp)
                        }
                        Text(
                            text = "${if (isPositive) "+" else "-"}${tx.amount} ${tx.currency}",
                            color = if (isPositive) AccentGreen else AccentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- Payments & Mobile Money SubView ---
@Composable
fun PaymentsView(viewModel: GaateViewModel) {
    var amountText by remember { mutableStateOf("") }
    var recipientText by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf("Wave") }
    var showSendDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("PAIEMENTS & ENVOIS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Envoyez vers Wave, Orange, MTN ou banques", color = TextGray, fontSize = 13.sp)
        }

        // Operator Selection Row
        item {
            Text("SÉLECTIONNEZ LE PRESTATAIRE", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val ops = listOf("Wave", "Orange Money", "MTN Money", "Moov", "Virement")
                ops.forEach { op ->
                    val isSel = op == selectedOperator
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedOperator = op }
                            .background(
                                if (isSel) VioletPremium else CardObsidian,
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, if (isSel) VioletLight else CardObsidianStroke, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            op,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Send Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardObsidianStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nouveau Transfert Instantané", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = recipientText,
                        onValueChange = { recipientText = it },
                        label = { Text("Numéro de téléphone / IBAN") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPremium,
                            focusedLabelColor = VioletLight,
                            unfocusedBorderColor = CardObsidianStroke,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payment_recipient"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Montant") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPremium,
                            focusedLabelColor = VioletLight,
                            unfocusedBorderColor = CardObsidianStroke,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showSendDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("payment_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium),
                        enabled = recipientText.isNotEmpty() && amountText.isNotEmpty()
                    ) {
                        Text("Confirmer l'Envoi", color = Color.White)
                    }
                }
            }
        }

        // Quick Bills Section
        item {
            Text("RÈGLEMENT DE FACTURES", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val bills = listOf(
                    Triple("Senelec Électricité", 8500.0, Icons.Default.ElectricBolt),
                    Triple("SDE Eau Sénégal", 4200.0, Icons.Default.WaterDrop),
                    Triple("Canal+ TV", 12000.0, Icons.Default.Tv)
                )
                
                bills.forEach { (bill, amt, icon) ->
                    Card(
                        onClick = { viewModel.payBill(amt, bill, "98231") },
                        colors = CardDefaults.cardColors(containerColor = CardObsidian),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardObsidianStroke.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(VioletPremium.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = VioletLight, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(bill, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Facture mensuelle récurrente", color = TextGray, fontSize = 11.sp)
                                }
                            }
                            Text(
                                text = String.format("%,.0f XOF", amt),
                                color = VioletLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSendDialog) {
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("Vérification PIN sécurisée", color = Color.White) },
            text = { Text("Confirmez-vous l'envoi de $amountText XOF à $recipientText via $selectedOperator ?", color = Color.White) },
            containerColor = SpaceObsidian,
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        viewModel.doSendMoney(amt, recipientText, selectedOperator)
                        showSendDialog = false
                        amountText = ""
                        recipientText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPremium)
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("Annuler", color = Color.White)
                }
            }
        )
    }
}

// --- African Marketplace SubView ---
@Composable
fun MarketplaceView(viewModel: GaateViewModel) {
    val cart by viewModel.cartItems.collectAsState()
    val search by viewModel.marketplaceSearchQuery.collectAsState()
    val products = viewModel.productsList.filter { it.title.lowercase().contains(search.lowercase()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("MARKETPLACE AFRICA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Découvrez l'artisanat et l'alimentation locale", color = TextGray, fontSize = 13.sp)
        
        Spacer(modifier = Modifier.height(12.dp))

        // Search text field
        OutlinedTextField(
            value = search,
            onValueChange = { viewModel.updateMarketplaceSearch(it) },
            placeholder = { Text("Rechercher un produit...", color = TextGray) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioletPremium,
                unfocusedBorderColor = CardObsidianStroke,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().testTag("marketplace_search")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Product Grid list
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { prod ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardObsidian),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardObsidianStroke)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dummy visual placeholder representation as Coil loading takes remote internet config
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VioletPremium.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.LocalMall, contentDescription = null, tint = VioletLight, modifier = Modifier.size(32.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(prod.description, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${prod.price} XOF", color = VioletLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Button(
                                        onClick = { viewModel.addToCart(prod) },
                                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("add_to_cart_${prod.id}")
                                    ) {
                                        Text("Acheter", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cart Drawer / Floating Sheet
        if (cart.isNotEmpty()) {
            val total = cart.sumOf { it.price * it.quantity }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = VioletPremium),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("VOTRE PANIER (${cart.size} art.)", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$total XOF", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.checkoutCart() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.testTag("checkout_marketplace")
                    ) {
                        Text("Payer Main", color = VioletPremium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Gaate AI integrated Assistant Screen ---
@Composable
fun GaateAIView(viewModel: GaateViewModel) {
    val aiMessages by viewModel.aiMessages.collectAsState()
    val aiInput by viewModel.aiInput.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom on load or list size change
    LaunchedEffect(aiMessages.size) {
        if (aiMessages.isNotEmpty()) {
            listState.animateScrollToItem(aiMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("GAATE AI ASSISTANT", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(AccentGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actif • Alimenté par Gemini 3.5", color = TextGray, fontSize = 11.sp)
                }
            }
            BirdLogo(size = 36f, tint = VioletPremium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Suggestion Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            val suggestions = listOf(
                "Analyse mon budget",
                "Quelles langues parles-tu ?",
                "Comment envoyer à Wave ?",
                "Est-ce sécurisé ?"
            )
            suggestions.forEach { sug ->
                Box(
                    modifier = Modifier
                        .clickable {
                            viewModel.aiInput.value = sug
                            viewModel.sendAiQuestion()
                        }
                        .background(CardObsidian, RoundedCornerShape(12.dp))
                        .border(1.dp, CardObsidianStroke, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(sug, color = Color.White, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conversational Chat Screen Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CardObsidian, RoundedCornerShape(16.dp))
                .border(1.dp, CardObsidianStroke, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(aiMessages) { (text, isMe) ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isMe) VioletPremium else SpaceBlack,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 16.dp
                            ),
                            border = BorderStroke(1.dp, if (isMe) VioletLight else CardObsidianStroke.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
                
                if (loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SpaceBlack)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = VioletPremium,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gaate AI réfléchit...", color = TextGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = aiInput,
                onValueChange = { viewModel.aiInput.value = it },
                placeholder = { Text("Posez votre question à Gaate AI...", color = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPremium,
                    unfocusedBorderColor = CardObsidianStroke,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f).testTag("ai_chat_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.sendAiQuestion() },
                modifier = Modifier
                    .size(52.dp)
                    .background(VioletPremium, RoundedCornerShape(12.dp))
                    .testTag("ai_send_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Envoyer", tint = Color.White)
            }
        }
    }
}

// --- Messenger / Chat private SubView ---
@Composable
fun MessengerView(viewModel: GaateViewModel) {
    val activeChannel by viewModel.activeChatChannel.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val input by viewModel.messageInput.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("MESSENGER GAATE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Échangez en toute sécurité avec vos proches et commerçants", color = TextGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Room Selectors
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val channels = listOf("support" to "Support", "dramane" to "Dramane Wave", "fatou" to "Fatou Wax")
            channels.forEach { (chan, label) ->
                val active = chan == activeChannel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectChatChannel(chan) }
                        .background(if (active) VioletPremium else CardObsidian, RoundedCornerShape(12.dp))
                        .border(1.dp, if (active) VioletLight else CardObsidianStroke, RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conversations block
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CardObsidian, RoundedCornerShape(16.dp))
                .border(1.dp, CardObsidianStroke, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages) { msg ->
                    val isMe = msg.isFromMe
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isMe) VioletPremium else SpaceBlack,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (!isMe) {
                                    Text(msg.senderName, color = VioletLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(msg.content, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Send row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.messageInput.value = it },
                placeholder = { Text("Écrire un message...", color = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPremium,
                    unfocusedBorderColor = CardObsidianStroke,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f).testTag("messenger_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.sendChatMessage() },
                modifier = Modifier
                    .size(52.dp)
                    .background(VioletPremium, RoundedCornerShape(12.dp))
                    .testTag("messenger_send")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Envoyer", tint = Color.White)
            }
        }
    }
}

// --- Interactive Taxi Booking Map View ---
@Composable
fun TaxiBookingView(viewModel: GaateViewModel) {
    val stage by viewModel.taxiStage.collectAsState()
    val progress by viewModel.taxiCarOffset.collectAsState()
    val dest by viewModel.taxiDestination.collectAsState()
    val rideType by viewModel.selectedRideType.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("GAATE RIDE / TAXI", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Réservez un chauffeur instantanément sur Dakar", color = TextGray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Simulated GPS Canvas (Live tracking representation)
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SpaceObsidian)
                .border(1.dp, CardObsidianStroke, RoundedCornerShape(16.dp))
        ) {
            // Animated Canvas representing Dakar street grid and active car
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Draw neon road grid lines
                drawLine(
                    color = CardObsidianStroke,
                    start = Offset(0f, canvasHeight * 0.3f),
                    end = Offset(canvasWidth, canvasHeight * 0.3f),
                    strokeWidth = 8f
                )
                drawLine(
                    color = CardObsidianStroke,
                    start = Offset(0f, canvasHeight * 0.7f),
                    end = Offset(canvasWidth, canvasHeight * 0.7f),
                    strokeWidth = 8f
                )
                drawLine(
                    color = CardObsidianStroke,
                    start = Offset(canvasWidth * 0.3f, 0f),
                    end = Offset(canvasWidth * 0.3f, canvasHeight),
                    strokeWidth = 8f
                )
                drawLine(
                    color = CardObsidianStroke,
                    start = Offset(canvasWidth * 0.7f, 0f),
                    end = Offset(canvasWidth * 0.7f, canvasHeight),
                    strokeWidth = 8f
                )

                // Draw user location point
                drawCircle(
                    color = AccentGreen,
                    radius = 16f,
                    center = Offset(canvasWidth * 0.3f, canvasHeight * 0.7f)
                )

                // Draw destination point
                drawCircle(
                    color = AccentRed,
                    radius = 16f,
                    center = Offset(canvasWidth * 0.7f, canvasHeight * 0.3f)
                )

                // Draw moving car representation
                if (stage == GaateViewModel.TaxiStage.DRIVING) {
                    // Interpolate car coordinates along route
                    val routeX = canvasWidth * 0.3f + (canvasWidth * 0.4f * progress)
                    val routeY = if (progress < 0.5f) {
                        canvasHeight * 0.7f
                    } else {
                        canvasHeight * 0.7f - (canvasHeight * 0.4f * (progress - 0.5f) * 2f)
                    }

                    drawCircle(
                        color = VioletLight,
                        radius = 20f,
                        center = Offset(routeX, routeY)
                    )
                }
            }

            // Stage Overlay Overlay Text
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .background(Color(0xCD000000), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = when (stage) {
                        GaateViewModel.TaxiStage.IDLE -> "Saisissez votre destination"
                        GaateViewModel.TaxiStage.BOOKING -> "Réservation en cours..."
                        GaateViewModel.TaxiStage.MATCHING -> "Recherche d'un chauffeur Gaate..."
                        GaateViewModel.TaxiStage.DRIVING -> "Course en cours... Live GPS"
                        GaateViewModel.TaxiStage.ARRIVED -> "Vous êtes arrivé à destination !"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interaction module
        Card(
            colors = CardDefaults.cardColors(containerColor = CardObsidian),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardObsidianStroke),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (stage == GaateViewModel.TaxiStage.IDLE || stage == GaateViewModel.TaxiStage.ARRIVED) {
                    OutlinedTextField(
                        value = dest,
                        onValueChange = { viewModel.taxiDestination.value = it },
                        label = { Text("Votre destination (Ex: Almadies, Dakar)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPremium,
                            unfocusedBorderColor = CardObsidianStroke,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("taxi_destination")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val types = listOf("Moto-Taxi", "Auto Classic", "Premium Black")
                        types.forEach { t ->
                            val sel = t == rideType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectedRideType.value = t }
                                    .background(if (sel) VioletPremium else SpaceBlack, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (sel) VioletLight else CardObsidianStroke, RoundedCornerShape(8.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(t, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.startTaxiBooking() },
                        modifier = Modifier.fillMaxWidth().testTag("taxi_book_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium),
                        enabled = dest.isNotEmpty()
                    ) {
                        Text("Réserver Ride (Simulé)")
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = VioletLight)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Suivi de votre course Gaate Ride", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Chauffeur ID: #GT-984 (Toyota Corolla)", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.resetTaxiSim() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                        ) {
                            Text("Annuler Course")
                        }
                    }
                }
            }
        }
    }
}

// --- Mini App Store SubView ---
@Composable
fun MiniAppsView(viewModel: GaateViewModel) {
    var runningMiniApp by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (runningMiniApp == null) {
            Text("MINI APP STORE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lancez des micro-services locaux instantanément", color = TextGray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(16.dp))

            val miniApps = listOf(
                Triple("SeneRead", "Lisez les journaux locaux et l'actualité sénégalaise en un clic.", Icons.Default.Newspaper),
                Triple("NiaExpress", "Commandez des plats faits maison par des cuisiniers locaux.", Icons.Default.Restaurant),
                Triple("Yassa Recipes", "Recettes authentiques de plats traditionnels africains.", Icons.Default.MenuBook)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(miniApps) { (name, desc, icon) ->
                    Card(
                        onClick = { runningMiniApp = name },
                        colors = CardDefaults.cardColors(containerColor = CardObsidian),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardObsidianStroke)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(VioletPremium.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = VioletLight, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, color = TextGray, fontSize = 12.sp)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
                        }
                    }
                }
            }
        } else {
            // Running MiniApp Container Mockup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { runningMiniApp = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Fermer", tint = Color.White)
                }
                Text("Mini App: $runningMiniApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Box(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BirdLogo(size = 48f, tint = VioletPremium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Conteneur de Sandbox Gaate One",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "L'application mini-service $runningMiniApp s'exécute en toute sécurité dans l'environnement Gaate.",
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { runningMiniApp = null },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium)
                    ) {
                        Text("Quitter la Mini App", color = Color.White)
                    }
                }
            }
        }
    }
}

// --- Pro / Business Console SubView ---
@Composable
fun BusinessView(viewModel: GaateViewModel) {
    val invoices by viewModel.businessInvoices.collectAsState()
    val client by viewModel.invoiceClient.collectAsState()
    val amount by viewModel.invoiceAmount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("ESPACE BUSINESS / PRO", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Créez vos factures et suivez votre comptabilité", color = TextGray, fontSize = 13.sp)
        }

        // Invoice Generator Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardObsidianStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nouvelle Facture Client", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = client,
                        onValueChange = { viewModel.invoiceClient.value = it },
                        label = { Text("Nom du client / Boutique") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPremium,
                            unfocusedBorderColor = CardObsidianStroke,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("business_client")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.invoiceAmount.value = it },
                        label = { Text("Montant (XOF)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletPremium,
                            unfocusedBorderColor = CardObsidianStroke,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("business_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.createBusinessInvoice() },
                        modifier = Modifier.fillMaxWidth().testTag("business_invoice_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium),
                        enabled = client.isNotEmpty() && amount.isNotEmpty()
                    ) {
                        Text("Créer Facture")
                    }
                }
            }
        }

        // Active invoice listing
        item {
            Text("HISTORIQUE DES FACTURES", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(invoices) { (name, amt) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = VioletLight)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text("$amt XOF", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Profile & KYC View SubPage ---
@Composable
fun ProfileView(viewModel: GaateViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val projectConfig by viewModel.projectConfig.collectAsState()

    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var pays by remember { mutableStateOf("") }
    var ville by remember { mutableStateOf("") }
    var num by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    var projName by remember { mutableStateOf("") }
    var founderName by remember { mutableStateOf("") }
    var contactNo by remember { mutableStateOf("") }
    var officialEmail by remember { mutableStateOf("") }
    var isAdminEditing by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        profile?.let {
            nom = it.nom
            prenom = it.prenom
            pays = it.pays
            ville = it.ville
            num = it.numero
            email = it.email
        }
    }

    LaunchedEffect(projectConfig) {
        projectConfig?.let {
            projName = it.projectName
            founderName = it.founderName
            contactNo = it.contactNo
            officialEmail = it.email
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("PROFIL & SÉCURITÉ", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Gérez vos données personnelles et conformité KYC", color = TextGray, fontSize = 13.sp)
        }

        // KYC Status Showcase Card
        item {
            val kycColor = if (profile?.kycStatus == "VERIFIED") AccentGreen else AccentOrange
            val kycLabel = if (profile?.kycStatus == "VERIFIED") "DOCUMENTS VALIDÉS" else "VÉRIFICATION PENDING"
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardObsidianStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONFORMITÉ KYC REGLEMENTAIRE", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .background(kycColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(kycLabel, color = kycColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Afin de lever les restrictions de transfert conformément aux régulations des banques centrales d'Afrique de l'Ouest, veuillez fournir une pièce d'identité.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.simulateKycUpload() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPremium)
                    ) {
                        Text("Soumettre Document ID (Scanner)", color = Color.White)
                    }
                }
            }
        }

        // Personal Info Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardObsidianStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INFORMATIONS PERSONNELLES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        TextButton(onClick = {
                            if (isEditing) {
                                viewModel.updateProfileInfo(nom, prenom, pays, ville, num, email, profile?.langue ?: "Français", profile?.devise ?: "XOF")
                            }
                            isEditing = !isEditing
                        }) {
                            Text(if (isEditing) "Sauvegarder" else "Modifier", color = VioletLight)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    val fields = listOf(
                        "Prénom" to prenom,
                        "Nom" to nom,
                        "Pays" to pays,
                        "Ville" to ville,
                        "Numéro" to num,
                        "Email" to email
                    )

                    fields.forEach { (label, value) ->
                        if (isEditing) {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { newValue ->
                                    when (label) {
                                        "Prénom" -> prenom = newValue
                                        "Nom" -> nom = newValue
                                        "Pays" -> pays = newValue
                                        "Ville" -> ville = newValue
                                        "Numéro" -> num = newValue
                                        "Email" -> email = newValue
                                    }
                                },
                                label = { Text(label) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletPremium,
                                    unfocusedBorderColor = CardObsidianStroke,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = TextGray, fontSize = 13.sp)
                                Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = CardObsidianStroke.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Secure Founder & Project Administration Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardObsidian),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VioletLight.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = VioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PANNEAU DE CONFIGURATION ADMIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        TextButton(onClick = {
                            if (isAdminEditing) {
                                viewModel.updateProjectConfig(projName, founderName, contactNo, officialEmail)
                            }
                            isAdminEditing = !isAdminEditing
                        }) {
                            Text(if (isAdminEditing) "Sauvegarder" else "Modifier", color = VioletLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Ces coordonnées officielles du fondateur sont stockées de manière hautement sécurisée dans la base de données Room locale de Gaate Dev AI et ne sont jamais exposées publiquement dans l'application.",
                        color = TextGray,
                        fontSize = 12.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val adminFields = listOf(
                        "Nom du Projet" to projName,
                        "Fondateur Officiel" to founderName,
                        "Contact Administrateur" to contactNo,
                        "Email Officiel" to officialEmail
                    )

                    adminFields.forEach { (label, value) ->
                        if (isAdminEditing) {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { newValue ->
                                    when (label) {
                                        "Nom du Projet" -> projName = newValue
                                        "Fondateur Officiel" -> founderName = newValue
                                        "Contact Administrateur" -> contactNo = newValue
                                        "Email Officiel" -> officialEmail = newValue
                                    }
                                },
                                label = { Text(label) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletPremium,
                                    unfocusedBorderColor = CardObsidianStroke,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = TextGray, fontSize = 13.sp)
                                Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = CardObsidianStroke.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
