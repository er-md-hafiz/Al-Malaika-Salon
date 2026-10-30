package com.almalika.salon.data.remote.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// ============================================================
// ROOT
// ============================================================
@Serializable
data class RemoteConfig(
    val metadata: Metadata = Metadata(schemaVersion = "1.0.0"),
    val appConfig: AppConfig = AppConfig(),
    val theme: ThemeConfig = ThemeConfig(),
    val startup: StartupConfig = StartupConfig(),
    val splashScreen: SplashScreenConfig? = null,
    val onboarding: OnboardingConfig? = null,
    val login: LoginConfig? = null,
    val pages: PagesConfig = PagesConfig(),
    val screens: List<ScreenConfig> = emptyList(),
    val navigation: NavigationConfig = NavigationConfig(),
    val home: HomeConfig = HomeConfig(),
    val featureFlags: FeatureFlags = FeatureFlags(),
    val remoteConfig: RemoteConfigMeta = RemoteConfigMeta(),
    val salon: Salon? = null,
    val banners: List<Banner> = emptyList(),
    val quickServices: List<Service> = emptyList(),
    val popularServices: List<Service> = emptyList(),
    val homeServices: List<HomeService> = emptyList(),
    val whyChooseUs: List<WhyChooseUsItem> = emptyList(),
    val homeServiceBooking: HomeServiceBookingConfig? = null,
    val franchise: FranchiseConfig? = null,
    val priceList: List<PriceListItem> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val barbers: List<Barber> = emptyList(),
    val galleryItems: List<GalleryItem> = emptyList()
)

// ============================================================
// METADATA
// ============================================================
@Serializable
data class Metadata(
    val schemaVersion: String,
    val uiConfigVersion: Int = 1,
    val lastUpdated: String? = null,
    val locale: String = "en-IN",
    val currencyCode: String = "INR",
    val currencySymbol: String? = null,
    val description: String? = null
)

// ============================================================
// APP CONFIG
// ============================================================
@Serializable
data class AppConfig(
    val appName: String = "",
    val tagline: String = "",
    val defaultLanguage: String = "en",
    val supportedLanguages: List<String> = listOf("en"),
    val minimumSupportedConfigVersion: Int = 1,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val forceUpdate: Boolean = false,
    val minimumAppVersion: String = "0.0.0",
    val showInternetStatus: Boolean = false
)

// ============================================================
// THEME
// ============================================================
@Serializable
data class ThemeConfig(
    val themeMode: String = "system",
    val darkModeEnabled: Boolean = true,
    val colors: ThemeColors = ThemeColors(),
    val typography: Typography = Typography(),
    val cornerRadiusDp: Int = 12,
    val buttonCornerRadiusDp: Int = 10,
    val useSystemStatusBarIcons: Boolean = false
)

@Serializable
data class ThemeColors(
    val primary: String? = null,
    val primaryDark: String? = null,
    val secondary: String? = null,
    val background: String? = null,
    val surface: String? = null,
    val textPrimary: String? = null,
    val textSecondary: String? = null,
    val buttonText: String? = null,
    val divider: String? = null,
    val error: String? = null,
    val success: String? = null,
    val onPrimary: String? = null,
    val onSurface: String? = null,
    val onError: String? = null,
    // Derived by ThemeEngine — never from JSON, but exposed in domain model:
    val onSecondary: String? = null,
    val onBackground: String? = null,
    val primaryContainer: String? = null,
    val onPrimaryContainer: String? = null,
    val secondaryContainer: String? = null,
    val onSecondaryContainer: String? = null,
    val surfaceVariant: String? = null,
    val onSurfaceVariant: String? = null,
    val outline: String? = null,
    val outlineVariant: String? = null,
    val scrim: String? = null,
    val inverseSurface: String? = null,
    val inverseOnSurface: String? = null,
    val inversePrimary: String? = null
)

@Serializable
data class Typography(
    val fontFamily: String = "default",
    val titleScale: Float = 1f,
    val bodyScale: Float = 1f
)

// ============================================================
// STARTUP / SPLASH / ONBOARDING / LOGIN
// ============================================================
@Serializable
data class StartupConfig(
    val splashScreenId: String = "splash",
    val onboardingScreenId: String = "onboarding",
    val loginScreenId: String = "login",
    val homeScreenId: String = "home",
    val defaultDestination: String = "home",
    val showOnboardingOnFirstLaunch: Boolean = true,
    val requireLogin: Boolean = false,
    val allowGuestBrowsing: Boolean = true,
    val fetchRemoteConfigAfterFirstFrame: Boolean = true
)

@Serializable
data class SplashScreenConfig(
    val id: String = "splash",
    val enabled: Boolean = true,
    val logoUrl: String? = null,
    val fallbackLogoResource: String? = "ic_launcher",
    val title: String = "",
    val tagline: String = "",
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val accentColor: String? = null,
    val logoAlignment: String = "center",
    val animationType: String = "fade",
    val minimumDurationMs: Long = 1000,
    val maximumLoadingDurationMs: Long = 5000,
    val showLoadingIndicator: Boolean = true,
    val nextDestination: String = "home"
)

@Serializable
data class OnboardingConfig(
    val id: String = "onboarding",
    val enabled: Boolean = true,
    val showOnlyOnFirstLaunch: Boolean = true,
    val allowSkip: Boolean = true,
    val showPageIndicator: Boolean = true,
    val nextButtonText: String = "Next",
    val previousButtonText: String = "Back",
    val skipButtonText: String = "Skip",
    val finishButtonText: String = "Get Started",
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val accentColor: String? = null,
    val completionDestination: String = "home",
    val pages: List<OnboardingPage> = emptyList()
)

@Serializable
data class OnboardingPage(
    val id: String,
    val order: Int = 0,
    val enabled: Boolean = true,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val imageContentDescription: String? = null,
    val backgroundColor: String? = null,
    val action: String = "next"
)

@Serializable
data class LoginConfig(
    val id: String = "login",
    val enabled: Boolean = true,
    val authMode: String = "firebase_email_link",
    val allowGuest: Boolean = true,
    val title: String = "",
    val subtitle: String = "",
    val logoUrl: String? = null,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val accentColor: String? = null,
    val showEmailLogin: Boolean = true,
    val showPasswordLogin: Boolean = false,
    val showPhoneLogin: Boolean = false,
    val showRegister: Boolean = true,
    val showForgotPassword: Boolean = false,
    val showGuestButton: Boolean = true,
    val guestButtonText: String = "Continue as Guest",
    val loginButtonText: String = "Sign In",
    val emailLinkButtonText: String = "Send Sign-In Link",
    val registerButtonText: String = "Create Account",
    val privacyPolicyUrl: String? = null,
    val termsUrl: String? = null,
    val registerDestination: String = "register",
    val emailLinkSentDestination: String = "email_link_sent",
    val successDestination: String = "home",
    val actions: List<LoginAction> = emptyList()
)

@Serializable
data class LoginAction(
    val id: String,
    val type: String,
    val title: String = "",
    val enabled: Boolean = true,
    val destination: String? = null
)

// ============================================================
// PAGES
// ============================================================
@Serializable
data class PagesConfig(
    val aboutUs: String? = null,
    val privacyPolicyUrl: String? = null,
    val termsUrl: String? = null
) {
    /** Resolves urlKey with priority: pages[key] → login[key] → null */
    fun resolve(urlKey: String?, login: LoginConfig? = null): String? = when (urlKey) {
        null -> null
        "aboutUs" -> aboutUs
        "privacyPolicyUrl" -> privacyPolicyUrl ?: login?.privacyPolicyUrl
        "termsUrl" -> termsUrl ?: login?.termsUrl
        else -> null
    }
}

// ============================================================
// SCREENS
// ============================================================
@Serializable
data class ScreenConfig(
    val id: String,
    val title: String = "",
    val type: String,
    val enabled: Boolean = true,
    val showInDrawer: Boolean = false,
    val showInBottomNavigation: Boolean = false,
    val requiresLogin: Boolean = false,
    val urlKey: String? = null,
    val icon: String? = null,
    val subtitle: String? = null
)

// ============================================================
// NAVIGATION
// ============================================================
@Serializable
data class NavigationConfig(
    val drawerEnabled: Boolean = true,
    val drawerHeader: DrawerHeader = DrawerHeader(),
    val drawerItems: List<NavItem> = emptyList(),
    val bottomNavigationEnabled: Boolean = true,
    val bottomNavigationMaxItems: Int = 5,
    val bottomItems: List<NavItem> = emptyList(),
    val invalidDestinationBehavior: String = "hide_item",
    val preserveBackStackPerTab: Boolean = true
)

@Serializable
data class DrawerHeader(
    val showLogo: Boolean = true,
    val logoUrl: String? = null,
    val title: String = "",
    val subtitle: String = "",
    val backgroundColor: String? = null,
    val textColor: String? = null
)

@Serializable
data class NavItem(
    val id: String,
    val title: String = "",
    val icon: String? = null,
    val selectedIcon: String? = null,
    val type: String = "navigate",
    val destination: String = "",
    val enabled: Boolean = true,
    val order: Int = 0
)

// ============================================================
// HOME
// ============================================================
@Serializable
data class HomeConfig(
    val enabled: Boolean = true,
    val screenTitle: String = "",
    val showToolbar: Boolean = true,
    val showDrawerButton: Boolean = true,
    val showGreeting: Boolean = false,
    val sections: List<HomeSection> = emptyList()
)

@Serializable
data class HomeSection(
    val id: String,
    val type: String,
    val title: String = "",
    val enabled: Boolean = true,
    val order: Int = 0,
    val dataSource: String? = null,
    val maxItems: Int? = null,
    val showViewAll: Boolean = false,
    val viewAllDestination: String? = null,
    val hideWhenEmpty: Boolean = false,
    val hideWhenDisabled: Boolean = false,
    val showAddress: Boolean = false,
    val showOpeningHours: Boolean = false,
    val itemAction: ConfigAction? = null,
    val action: ConfigAction? = null
)

@Serializable
data class ConfigAction(
    val type: String = "navigate",
    val destination: String? = null
)

// ============================================================
// FEATURE FLAGS
// ============================================================
@Serializable
data class FeatureFlags(
    val onboarding: Boolean = true,
    val login: Boolean = true,
    val guestMode: Boolean = true,
    val homeBanners: Boolean = true,
    val quickServices: Boolean = true,
    val popularServices: Boolean = true,
    val priceList: Boolean = true,
    val gallery: Boolean = true,
    val offers: Boolean = true,
    val reviews: Boolean = true,
    val barberSelection: Boolean = false,
    val homeServiceBooking: Boolean = true,
    val franchise: Boolean = true,
    val darkMode: Boolean = true,
    val notifications: Boolean = true,
    val remoteConfigRefresh: Boolean = true,
    val passwordlessEmailLink: Boolean = true,
    val emailRegistration: Boolean = true,
    val guestAccountLinking: Boolean = true
)

// ============================================================
// REMOTE CONFIG META
// ============================================================
@Serializable
data class RemoteConfigMeta(
    val url: String = "",
    val enabled: Boolean = true,
    val refreshOnLaunch: Boolean = true,
    val refreshIntervalHours: Int = 6,
    val connectTimeoutSeconds: Int = 10,
    val readTimeoutSeconds: Int = 15,
    val useBundledFallback: Boolean = true,
    val useLastKnownGoodConfigOffline: Boolean = true,
    val cacheOnlyValidatedConfig: Boolean = true,
    val applyUpdatesAtomically: Boolean = true,
    val checkSchemaVersion: Boolean = true,
    val allowUnknownFields: Boolean = true,
    val allowArbitraryCode: Boolean = false
)

// ============================================================
// SALON
// ============================================================
@Serializable
data class Salon(
    val id: Int = 0,
    val name: String = "",
    val about: String = "",
    val tagline: String = "",
    val address: String = "",
    val mobileNumber: String = "",
    val mobileNumberDial: String = "",
    val email: String = "",
    val whatsappUrl: String? = null,
    val rating: Float = 0f,
    val totalReviews: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val openingHours: String = "",
    val workingDays: String = "",
    val mapsEmbedUrl: String? = null,
    val mapsSearchUrl: String? = null,
    val playStoreUrl: String? = null,
    val social: Social = Social()
)

@Serializable
data class Social(
    val instagram: String? = null,
    val facebook: String? = null
)

// ============================================================
// CONTENT ITEMS
// ============================================================
object BannerIdSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BannerId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        val intVal = value.toIntOrNull()
        if (intVal != null) {
            encoder.encodeInt(intVal)
        } else {
            encoder.encodeString(value)
        }
    }

    override fun deserialize(decoder: Decoder): String {
        return try {
            decoder.decodeInt().toString()
        } catch (_: Exception) {
            decoder.decodeString()
        }
    }
}

@Serializable
data class Banner(
    @Serializable(with = BannerIdSerializer::class)
    val id: String = "0",
    val imageUrl: String? = null,
    val title: String = "",
    val subtitle: String = "",
    val category: String? = null,
    val action: ConfigAction? = null
)

@Serializable
data class Service(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String? = null
)

@Serializable
data class HomeService(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val imageUrl: String? = null,
    val category: String? = null,
    val serviceType: String = "HOME",
    val isAvailable: Boolean = true
)

@Serializable
data class WhyChooseUsItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val icon: String? = null
)

@Serializable
data class PriceListItem(
    val id: Int = 0,
    val name: String = "",
    val price: Int = 0,
    val category: String? = null,
    val description: String = ""
)

@Serializable
data class Offer(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val price: Int = 0,
    val originalPrice: Int = 0,
    val badge: String? = null,
    val validUntil: String? = null,
    val enabled: Boolean = true
)

@Serializable
data class Review(
    val id: Int = 0,
    val customerName: String = "",
    val rating: Int = 0,
    val reviewText: String = "",
    val date: String? = null,
    val source: String = "customer",
    val isVerified: Boolean = false
) {
    val isSample: Boolean get() = source == "demo" || !isVerified
}

@Serializable
data class Barber(
    val id: Int = 0,
    val name: String = "",
    val expertise: String = "",
    val imageUrl: String? = null
)

@Serializable
data class GalleryItem(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val category: String? = null,
    val isAvailable: Boolean = true
)

// ============================================================
// BOOKING
// ============================================================
@Serializable
data class HomeServiceBookingConfig(
    val enabled: Boolean = true,
    val minimumBookingAmount: Int = 0,
    val serviceCharge: Int = 0,
    val freeServiceChargeAbove: Int = 0,
    val advanceBookingHours: Int = 0,
    val bookingMessage: String = "",
    val availableDays: List<String> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList()
)

@Serializable
data class TimeSlot(
    val id: Int = 0,
    val time: String = "",
    val isAvailable: Boolean = true
)

// ============================================================
// FRANCHISE
// ============================================================
@Serializable
data class FranchiseConfig(
    val id: String = "franchise",
    val enabled: Boolean = true,
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val ctaText: String = "",
    val ctaUrl: String? = null,
    val formUrl: String? = null
)

