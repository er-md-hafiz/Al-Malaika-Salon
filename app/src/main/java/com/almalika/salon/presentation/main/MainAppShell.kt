package com.almalika.salon.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.almalika.salon.domain.model.AuthState
import com.almalika.salon.domain.model.UserSessionState
import androidx.hilt.navigation.compose.hiltViewModel
import com.almalika.salon.presentation.profile.ProfileScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.almalika.salon.core.action.ActionExecutor
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.NavItem
import com.almalika.salon.domain.model.SalonMasterConfig
import com.almalika.salon.presentation.about.AboutScreen
import com.almalika.salon.presentation.booking.BookingScreen
import com.almalika.salon.presentation.booking.BookingViewModel
import com.almalika.salon.presentation.common.ErrorRetryView
import com.almalika.salon.presentation.common.ShimmerCard
import com.almalika.salon.presentation.contact.ContactScreen
import com.almalika.salon.presentation.gallery.GalleryScreen
import com.almalika.salon.presentation.gallery.GalleryViewModel
import com.almalika.salon.presentation.home.HomeScreen
import com.almalika.salon.presentation.home.HomeViewModel
import com.almalika.salon.presentation.homeservice.HomeServiceScreen
import com.almalika.salon.presentation.homeservice.HomeServiceViewModel
import com.almalika.salon.presentation.legal.PrivacyScreen
import com.almalika.salon.presentation.legal.TermsScreen
import com.almalika.salon.presentation.locations.LocationsScreen
import com.almalika.salon.presentation.locations.LocationsViewModel
import com.almalika.salon.presentation.navigation.IconResolver
import com.almalika.salon.presentation.offers.OffersScreen
import com.almalika.salon.presentation.offers.OffersViewModel
import com.almalika.salon.presentation.reviews.ReviewsScreen
import com.almalika.salon.presentation.reviews.ReviewsViewModel
import com.almalika.salon.presentation.services.ServicesScreen
import com.almalika.salon.presentation.services.ServicesViewModel
import com.almalika.salon.presentation.settings.SettingsScreen
import com.almalika.salon.presentation.login.LoginScreen
import com.almalika.salon.presentation.register.RegisterScreen
import com.almalika.salon.presentation.forgotpassword.ForgotPasswordScreen
import com.almalika.salon.presentation.account.AccountScreen
import com.almalika.salon.presentation.auth.AuthViewModel
import com.almalika.salon.presentation.onboarding.OnboardingScreen
import com.almalika.salon.presentation.splash.SplashScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    viewModel: MainViewModel = hiltViewModel(),
    initialRoute: String? = null,
    onRouteHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val shellState by viewModel.uiState.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    when (val state = shellState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            ErrorRetryView(
                message = state.message,
                onRetry = {}
            )
        }

        is UiState.Success -> {
            val data = state.data
            val master = data.masterConfig
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val sessionState by authViewModel.sessionState.collectAsState()

            val currentAuthUser = (authState as? AuthState.Authenticated)?.user

            val activeBottomNav = remember(master.bottomNavigation) {
                master.bottomNavigation
                    .filter { it.enabled }
                    .sortedBy { it.order }
            }

            val activeDrawer = remember(master.drawer, sessionState) {
                val isAccountItem: (NavItem) -> Boolean = { item ->
                    item.id in listOf("drawer_account", "account", "login") ||
                            item.route in listOf("login", "register", "account") ||
                            item.title.contains("Sign In", ignoreCase = true) ||
                            item.title.contains("Register", ignoreCase = true)
                }

                val nonAccountItems = master.drawer.filter { item ->
                    !isAccountItem(item) && item.enabled
                }

                val result = nonAccountItems.toMutableList()

                // Runtime authentication-dependent drawer visibility:
                // Signed out or Anonymous guest -> "Sign In / Register" MUST be visible
                // Registered user -> "Sign In / Register" MUST be hidden
                if (sessionState != UserSessionState.REGISTERED_USER) {
                    val existingAccountItem = master.drawer.firstOrNull { isAccountItem(it) }
                    val drawerAccountItem = existingAccountItem?.copy(
                        enabled = true,
                        title = "Sign In / Register",
                        route = "login"
                    ) ?: NavItem(
                        id = "drawer_account",
                        title = "Sign In / Register",
                        icon = "account_circle",
                        route = "login",
                        enabled = true,
                        order = (master.drawer.maxOfOrNull { it.order } ?: 13) + 1
                    )
                    result.add(drawerAccountItem)
                }

                result.sortedBy { it.order }
            }

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "splash"
            val isRootFlowScreen = currentRoute in listOf("splash", "onboarding", "login", "register", "forgot_password")

            val startDestination = remember {
                if (master.splashScreen.enabled) {
                    "splash"
                } else if (master.onboarding.enabled && !isOnboardingCompleted) {
                    "onboarding"
                } else if (master.startup.requireLogin && authState !is AuthState.Authenticated) {
                    "login"
                } else {
                    "home"
                }
            }

            // Log screen view to Firebase Analytics and Crashlytics whenever route changes
            LaunchedEffect(currentRoute) {
                viewModel.logScreen(currentRoute)
            }

            // Handle FCM notification deep link navigation
            LaunchedEffect(initialRoute) {
                if (!initialRoute.isNullOrBlank() && initialRoute != "home" && !isRootFlowScreen) {
                    try {
                        navController.navigate(initialRoute) {
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        viewModel.crashlyticsHelper.recordException(e)
                    } finally {
                        onRouteHandled()
                    }
                }
            }

            val handleAction: (AppAction) -> Unit = { action ->
                viewModel.logAction(action)
                when (action) {
                    is AppAction.Home -> navController.navigate("home") { launchSingleTop = true }
                    is AppAction.Services -> navController.navigate("services") { launchSingleTop = true }
                    is AppAction.Offers -> navController.navigate("offers") { launchSingleTop = true }
                    is AppAction.Reviews -> navController.navigate("reviews") { launchSingleTop = true }
                    is AppAction.Booking -> navController.navigate("booking") { launchSingleTop = true }
                    is AppAction.HomeService -> navController.navigate("home_service") { launchSingleTop = true }
                    is AppAction.Gallery -> navController.navigate("gallery") { launchSingleTop = true }
                    is AppAction.Locations -> navController.navigate("locations") { launchSingleTop = true }
                    is AppAction.About -> navController.navigate("about") { launchSingleTop = true }
                    is AppAction.Contact -> navController.navigate("contact") { launchSingleTop = true }
                    is AppAction.Settings -> navController.navigate("settings") { launchSingleTop = true }
                    is AppAction.Profile -> navController.navigate("profile") { launchSingleTop = true }
                    is AppAction.Terms -> navController.navigate("terms") { launchSingleTop = true }
                    is AppAction.Privacy -> navController.navigate("privacy") { launchSingleTop = true }
                    is AppAction.Login -> navController.navigate("login") { launchSingleTop = true }
                    is AppAction.Register -> navController.navigate("register") { launchSingleTop = true }
                    is AppAction.ForgotPassword -> navController.navigate("forgot_password") { launchSingleTop = true }
                    is AppAction.Account -> navController.navigate("account") { launchSingleTop = true }
                    else -> ActionExecutor.execute(context, action)
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = !isRootFlowScreen && master.navigation.drawerEnabled,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight(),
                        windowInsets = WindowInsets.statusBars
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .navigationBarsPadding()
                                .padding(bottom = 24.dp)
                        ) {
                            // Royal Salon Branding Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (sessionState) {
                                            UserSessionState.REGISTERED_USER -> Icons.Default.AccountCircle
                                            UserSessionState.ANONYMOUS_GUEST -> Icons.Default.Person
                                            UserSessionState.SIGNED_OUT -> Icons.Default.ContentCut
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    val titleText = when (sessionState) {
                                        UserSessionState.REGISTERED_USER -> currentAuthUser?.displayName?.ifBlank { "Registered Member" } ?: "Registered Member"
                                        UserSessionState.ANONYMOUS_GUEST -> "Guest User"
                                        UserSessionState.SIGNED_OUT -> master.salon.name.ifBlank { "Al-Malaika Salon" }
                                    }
                                    val subtitleText = when (sessionState) {
                                        UserSessionState.REGISTERED_USER -> currentAuthUser?.email?.ifBlank { "Active Member Account" } ?: "Active Member Account"
                                        UserSessionState.ANONYMOUS_GUEST -> "Guest"
                                        UserSessionState.SIGNED_OUT -> "No authenticated user"
                                    }
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = subtitleText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            activeDrawer.forEach { drawerItem ->
                                NavigationDrawerItem(
                                    label = { Text(drawerItem.title) },
                                    icon = {
                                        Icon(
                                            imageVector = IconResolver.resolve(drawerItem.icon),
                                            contentDescription = drawerItem.title
                                        )
                                    },
                                    selected = currentRoute == drawerItem.route,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        if (drawerItem.route.startsWith("action:")) {
                                            when (drawerItem.route) {
                                                "action:share" -> handleAction(AppAction.ShareApp(master.share.message, master.share.playStoreUrl))
                                                "action:rate" -> handleAction(AppAction.RateApp)
                                            }
                                        } else {
                                            navController.navigate(drawerItem.route) { launchSingleTop = true }
                                        }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "v${master.profile?.appVersion.orEmpty().ifBlank { "1.0.0" }} • Al-Malaika Salon",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        if (!isRootFlowScreen) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = getScreenTitle(currentRoute, master),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                navigationIcon = {
                                    if (master.navigation.drawerEnabled) {
                                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Navigation Menu")
                                        }
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!isRootFlowScreen && master.navigation.bottomNavigationEnabled && activeBottomNav.isNotEmpty()) {
                            NavigationBar {
                                activeBottomNav.forEach { navItem ->
                                    val isSelected = currentRoute == navItem.route || (navItem.route == "profile" && currentRoute in listOf("profile", "settings", "account"))
                                    val iconToUse = if (isSelected && navItem.selectedIcon.isNotBlank()) navItem.selectedIcon else navItem.icon
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = IconResolver.resolve(iconToUse),
                                                contentDescription = navItem.title
                                            )
                                        },
                                        label = { Text(navItem.title) },
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != navItem.route) {
                                                navController.navigate(navItem.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                config = master.splashScreen,
                                onFinish = {
                                    val nextRoute = when {
                                        master.onboarding.enabled && !isOnboardingCompleted -> "onboarding"
                                        master.startup.requireLogin && authState !is AuthState.Authenticated -> "login"
                                        else -> "home"
                                    }
                                    navController.navigate(nextRoute) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("onboarding") {
                            OnboardingScreen(
                                config = master.onboarding,
                                onComplete = {
                                    viewModel.completeOnboarding()
                                    val nextRoute = if (master.startup.requireLogin && authState !is AuthState.Authenticated) "login" else "home"
                                    navController.navigate(nextRoute) {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("login") {
                            val authVm: AuthViewModel = hiltViewModel()
                            LoginScreen(
                                config = master.login,
                                viewModel = authVm,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onRegisterClick = {
                                    navController.navigate("register") { launchSingleTop = true }
                                },
                                onForgotPasswordClick = {
                                    navController.navigate("forgot_password") { launchSingleTop = true }
                                },
                                onGuestLogin = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onOpenTerms = { handleAction(AppAction.Terms) },
                                onOpenPrivacy = { handleAction(AppAction.Privacy) }
                            )
                        }

                        composable("register") {
                            val authVm: AuthViewModel = hiltViewModel()
                            RegisterScreen(
                                config = master.register,
                                viewModel = authVm,
                                onRegisterSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onLoginClick = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onOpenTerms = { handleAction(AppAction.Terms) },
                                onOpenPrivacy = { handleAction(AppAction.Privacy) }
                            )
                        }

                        composable("forgot_password") {
                            val authVm: AuthViewModel = hiltViewModel()
                            ForgotPasswordScreen(
                                config = master.forgotPassword,
                                viewModel = authVm,
                                onLoginClick = {
                                    navController.navigate("login") {
                                        popUpTo("forgot_password") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("account") {
                            val authVm: AuthViewModel = hiltViewModel()
                            AccountScreen(
                                master = master,
                                viewModel = authVm,
                                onAction = handleAction,
                                onSignedOut = {
                                    authViewModel.clearAllActionStates()
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val homeVm: HomeViewModel = hiltViewModel()
                            HomeScreen(
                                viewModel = homeVm,
                                masterConfig = master,
                                services = data.services,
                                offers = data.offers,
                                galleryImages = data.galleryImages,
                                branches = data.branches,
                                onAction = handleAction
                            )
                        }

                        composable("services") {
                            val servicesVm: ServicesViewModel = hiltViewModel()
                            ServicesScreen(viewModel = servicesVm, onAction = handleAction)
                        }

                        composable("price_list") {
                            val servicesVm: ServicesViewModel = hiltViewModel()
                            ServicesScreen(viewModel = servicesVm, onAction = handleAction)
                        }

                        composable("prices") {
                            val servicesVm: ServicesViewModel = hiltViewModel()
                            ServicesScreen(viewModel = servicesVm, onAction = handleAction)
                        }

                        composable("offers") {
                            val offersVm: OffersViewModel = hiltViewModel()
                            OffersScreen(viewModel = offersVm, onAction = handleAction)
                        }

                        composable("reviews") {
                            val reviewsVm: ReviewsViewModel = hiltViewModel()
                            ReviewsScreen(viewModel = reviewsVm, master = master, onAction = handleAction)
                        }

                        composable("gallery") {
                            val galleryVm: GalleryViewModel = hiltViewModel()
                            GalleryScreen(viewModel = galleryVm, onAction = handleAction)
                        }

                        composable("locations") {
                            val locationsVm: LocationsViewModel = hiltViewModel()
                            LocationsScreen(viewModel = locationsVm, onAction = handleAction)
                        }

                        composable("booking") {
                            val bookingVm: BookingViewModel = hiltViewModel()
                            BookingScreen(viewModel = bookingVm, onAction = handleAction)
                        }

                        composable("home_service") {
                            val homeServiceVm: HomeServiceViewModel = hiltViewModel()
                            HomeServiceScreen(viewModel = homeServiceVm, onAction = handleAction)
                        }

                        composable("about") {
                            AboutScreen(master = master, onAction = handleAction)
                        }

                        composable("contact") {
                            ContactScreen(master = master, onAction = handleAction)
                        }

                        composable("profile") {
                            val userDarkMode by viewModel.isDarkMode.collectAsState()
                            val currentIsDark = userDarkMode ?: (master.theme.themeMode.lowercase() != "light")
                            ProfileScreen(
                                master = master,
                                authViewModel = authViewModel,
                                isDarkMode = currentIsDark,
                                onToggleDarkMode = { viewModel.setDarkMode(it) },
                                onAction = handleAction,
                                onSignedOut = {
                                    authViewModel.clearAllActionStates()
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("settings") {
                            val userDarkMode by viewModel.isDarkMode.collectAsState()
                            val currentIsDark = userDarkMode ?: (master.theme.themeMode.lowercase() != "light")
                            ProfileScreen(
                                master = master,
                                authViewModel = authViewModel,
                                isDarkMode = currentIsDark,
                                onToggleDarkMode = { viewModel.setDarkMode(it) },
                                onAction = handleAction,
                                onSignedOut = {
                                    authViewModel.clearAllActionStates()
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("terms") {
                            TermsScreen(master = master, onAction = handleAction)
                        }

                        composable("privacy") {
                            PrivacyScreen(master = master, onAction = handleAction)
                        }
                    }
                }
            }
        }
    }
}

fun getScreenTitle(route: String, master: SalonMasterConfig): String {
    return when (route) {
        "home" -> master.salon.name.ifBlank { "Al-Malaika Salon" }
        "services" -> "Our Services"
        "price_list", "prices" -> "Price List"
        "offers" -> "Exclusive Offers"
        "reviews" -> "Client Reviews"
        "gallery" -> "Salon Gallery"
        "locations" -> "Our Branches"
        "booking" -> "Book Appointment"
        "home_service" -> "Home Service"
        "about" -> "About Us"
        "contact" -> "Contact Us"
        "profile" -> "Profile & Settings"
        "settings" -> "Settings"
        "terms" -> "Terms & Conditions"
        "privacy" -> "Privacy Policy"
        "login" -> master.login.title.ifBlank { "Sign In" }
        "register" -> master.register.title.ifBlank { "Create Account" }
        "forgot_password" -> master.forgotPassword.title.ifBlank { "Reset Password" }
        "account" -> "My Account"
        else -> master.salon.name
    }
}
