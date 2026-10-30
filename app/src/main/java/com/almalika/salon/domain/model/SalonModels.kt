package com.almalika.salon.domain.model

data class SalonMasterConfig(
    val schemaVersion: Int,
    val configVersion: Int,
    val salon: SalonInfo,
    val bottomNavigation: List<NavItem>,
    val drawer: List<NavItem>,
    val featureFlags: Map<String, Boolean>,
    val whatsapp: WhatsAppConfig,
    val contact: ContactInfo,
    val share: ShareConfig,
    val legal: LegalConfig,
    val appConfig: AppConfig = AppConfig(),
    val profile: ProfileConfig? = null,
    val franchise: FranchiseConfig? = null,
    val startup: StartupConfig = StartupConfig(),
    val splashScreen: SplashScreenConfig = SplashScreenConfig(),
    val onboarding: OnboardingConfig = OnboardingConfig(),
    val login: LoginConfig = LoginConfig(),
    val register: RegisterConfig = RegisterConfig(),
    val forgotPassword: ForgotPasswordConfig = ForgotPasswordConfig(),
    val authentication: AuthenticationConfig = AuthenticationConfig(),
    val navigation: NavigationConfig = NavigationConfig(),
    val theme: ThemeConfig = ThemeConfig(),
    val pages: Map<String, String> = emptyMap(),
    val screens: List<ScreenSpec> = emptyList(),
    val remoteConfig: RemoteConfig = RemoteConfig(),
    val whyChooseUs: List<WhyChooseUsItem> = emptyList()
)

data class ScreenSpec(
    val id: String,
    val title: String,
    val type: String,
    val enabled: Boolean = true,
    val showInDrawer: Boolean = false,
    val showInBottomNavigation: Boolean = false,
    val requiresLogin: Boolean = false,
    val urlKey: String? = null,
    val icon: String? = null,
    val subtitle: String? = null
)

data class AppConfig(
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "We will be back soon. Please try again later.",
    val forceUpdate: Boolean = false,
    val minimumAppVersion: String = "1.0.0",
    val showInternetStatus: Boolean = false,
    val minimumSupportedConfigVersion: Int = 1
)

data class RemoteConfig(
    val url: String = "https://er-md-hafiz.github.io/api.almalaikasalon/menu.json",
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
    val onError: String? = null
)

data class ThemeConfig(
    val themeMode: String = "dark",
    val primaryColor: String = "#D4AF37",
    val secondaryColor: String = "#A67C1E",
    val backgroundColor: String = "#18181B",
    val surfaceColor: String = "#27272A",
    val textColor: String = "#FFFFFF",
    val colors: ThemeColors? = null,
    val cornerRadiusDp: Int = 12,
    val buttonCornerRadiusDp: Int = 10,
    val useSystemStatusBarIcons: Boolean = false
)

data class StartupConfig(
    val splashScreenId: String = "splash",
    val onboardingScreenId: String = "onboarding",
    val loginScreenId: String = "login",
    val homeScreenId: String = "home",
    val requireLogin: Boolean = false
)

data class SplashScreenConfig(
    val enabled: Boolean = true,
    val logoUrl: String = "",
    val title: String = "",
    val tagline: String = "",
    val backgroundColor: String = "#121212",
    val textColor: String = "#FFFFFF",
    val accentColor: String = "#D4AF37",
    val minimumDurationMs: Long = 1500L,
    val animationType: String = "fade"
)

data class OnboardingConfig(
    val enabled: Boolean = true,
    val showSkip: Boolean = true,
    val pages: List<OnboardingPage> = emptyList()
)

data class OnboardingPage(
    val id: String,
    val order: Int,
    val title: String,
    val description: String,
    val illustrationUrl: String,
    val nextLabel: String = "Next",
    val skipLabel: String = "Skip",
    val getStartedLabel: String = "Get Started"
)

data class LoginConfig(
    val id: String = "login",
    val enabled: Boolean = true,
    val authMode: String = "firebase_email_link",
    val allowGuest: Boolean = true,
    val title: String = "Welcome Back",
    val subtitle: String = "Sign in to your Al-Malaika Salon account.",
    val logoUrl: String = "",
    val backgroundColor: String = "#101010",
    val textColor: String = "#FFFFFF",
    val accentColor: String = "#D4AF37",
    val showEmailLogin: Boolean = true,
    val showPasswordLogin: Boolean = false,
    val showPhoneLogin: Boolean = false,
    val showRegister: Boolean = true,
    val showForgotPassword: Boolean = true,
    val showGuestButton: Boolean = true,
    val guestButtonText: String = "Continue as Guest",
    val loginButtonText: String = "Sign In",
    val emailLinkButtonText: String = "Send Sign-In Link",
    val registerButtonText: String = "Create Account",
    val forgotPasswordButtonText: String = "Forgot Password?",
    val privacyPolicyUrl: String = "https://er-md-hafiz.github.io/api.almalaikasalon/privacy.html",
    val termsUrl: String = "https://er-md-hafiz.github.io/api.almalaikasalon/terms.html",
    val registerDestination: String = "register",
    val forgotPasswordDestination: String = "forgot_password",
    val successDestination: String = "home",
    val phonePlaceholder: String = "Enter 10-digit mobile number",
    val continueAsGuestLabel: String = "Continue as Guest",
    val loginButtonLabel: String = "Sign In"
)

data class RegisterConfig(
    val id: String = "register",
    val enabled: Boolean = true,
    val title: String = "Create Your Account",
    val subtitle: String = "Register with your email address.",
    val showFullName: Boolean = true,
    val showEmail: Boolean = true,
    val showPassword: Boolean = true,
    val showConfirmPassword: Boolean = true,
    val requireTermsAcceptance: Boolean = true,
    val sendEmailVerification: Boolean = true,
    val termsUrl: String = "https://er-md-hafiz.github.io/api.almalaikasalon/terms.html",
    val privacyPolicyUrl: String = "https://er-md-hafiz.github.io/api.almalaikasalon/privacy.html",
    val loginDestination: String = "login",
    val successDestination: String = "home"
)

data class ForgotPasswordConfig(
    val id: String = "forgot_password",
    val enabled: Boolean = true,
    val title: String = "Reset Your Password",
    val subtitle: String = "Enter your email address to request a password reset.",
    val emailRequired: Boolean = true,
    val loginDestination: String = "login"
)

data class AuthenticationConfig(
    val provider: String = "firebase",
    val emailPasswordEnabled: Boolean = true,
    val anonymousEnabled: Boolean = true,
    val phoneAuthEnabled: Boolean = false,
    val guestAccountLinkingEnabled: Boolean = true,
    val persistSession: Boolean = true
)

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String? = null,
    val isAnonymous: Boolean = false,
    val isEmailVerified: Boolean = false,
    val photoUrl: String? = null
)

data class UserPreferences(
    val darkMode: Boolean = true,
    val notifications: Boolean = true
)

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val accountType: String = "registered",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val preferences: UserPreferences = UserPreferences()
)

enum class UserSessionState {
    SIGNED_OUT,
    ANONYMOUS_GUEST,
    REGISTERED_USER
}

sealed interface AuthState {
    data object Initial : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: AuthUser, val profile: UserProfile? = null) : AuthState
    data object Unauthenticated : AuthState
}

val AuthState.sessionState: UserSessionState
    get() = when (this) {
        is AuthState.Authenticated -> if (user.isAnonymous) UserSessionState.ANONYMOUS_GUEST else UserSessionState.REGISTERED_USER
        else -> UserSessionState.SIGNED_OUT
    }

val AuthState.isRegisteredUser: Boolean
    get() = this is AuthState.Authenticated && !this.user.isAnonymous

val AuthState.isAnonymousGuest: Boolean
    get() = this is AuthState.Authenticated && this.user.isAnonymous

val AuthState.isSignedOut: Boolean
    get() = this !is AuthState.Authenticated

val AuthUser.sessionState: UserSessionState
    get() = if (isAnonymous) UserSessionState.ANONYMOUS_GUEST else UserSessionState.REGISTERED_USER

data class NavigationConfig(
    val drawerEnabled: Boolean = true,
    val bottomNavigationEnabled: Boolean = true,
    val bottomNavigationMaxItems: Int = 5,
    val drawerItems: List<NavItem> = emptyList(),
    val bottomItems: List<NavItem> = emptyList()
)

data class SalonInfo(
    val name: String,
    val tagline: String,
    val logoUrl: String,
    val phone: String,
    val whatsapp: String,
    val address: String,
    val aboutText: String
)

data class NavItem(
    val id: String,
    val title: String,
    val icon: String,
    val route: String,
    val enabled: Boolean,
    val order: Int,
    val selectedIcon: String = icon
)

data class WhatsAppConfig(
    val enabled: Boolean,
    val number: String,
    val appointmentMessageTemplate: String,
    val homeServiceMessageTemplate: String
)

data class ContactInfo(
    val phone: String,
    val whatsapp: String,
    val email: String,
    val website: String,
    val instagram: String,
    val facebook: String,
    val youtube: String,
    val googleMaps: String
)

data class ShareConfig(
    val enabled: Boolean,
    val message: String,
    val playStoreUrl: String
)

data class LegalConfig(
    val termsUrl: String,
    val privacyUrl: String,
    val aboutUsUrl: String = ""
)

data class ProfileConfig(
    val appName: String,
    val appVersion: String,
    val shareMessage: String,
    val supportEmail: String,
    val supportPhone: String,
    val privacyPolicyUrl: String,
    val termsUrl: String,
    val aboutUs: String,
    val darkModeEnabled: Boolean,
    val notificationEnabled: Boolean,
    val menuItems: List<ProfileMenuItem> = emptyList()
)

data class ProfileMenuItem(
    val id: String,
    val title: String,
    val type: String,
    val enabled: Boolean,
    val icon: String
)

data class FranchiseConfig(
    val enabled: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val bannerImage: String = "",
    val contactPerson: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val whatsApp: String = "",
    val website: String = ""
)

data class HomeConfig(
    val salon: SalonInfo,
    val sections: List<HomeSection>
)

enum class SectionType {
    HERO,
    QUICK_ACTIONS,
    SERVICES,
    OFFERS,
    GALLERY,
    LOCATIONS,
    ABOUT,
    CONTACT,
    BANNER,
    REVIEWS,
    SPACER,
    FEATURE_GRID,
    FRANCHISE_BANNER,
    UNKNOWN
}

data class HomeBanner(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val subtitle: String = "",
    val category: String = "",
    val serviceType: String = "",
    val isAvailable: Boolean = true,
    val actionRoute: String = ""
)

data class HomeSection(
    val id: String,
    val type: SectionType,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val actionText: String,
    val actionRoute: String,
    val sourceUrl: String,
    val itemsToShow: Int,
    val enabled: Boolean,
    val order: Int,
    val banners: List<HomeBanner> = emptyList(),
    val autoSlideIntervalMs: Long = 4000L,
    val dataSource: String? = null,
    val showViewAll: Boolean = true,
    val viewAllDestination: String? = null,
    val hideWhenEmpty: Boolean = false,
    val hideWhenDisabled: Boolean = false,
    val showAddress: Boolean = false,
    val showOpeningHours: Boolean = false
)

data class ServiceCategory(
    val id: String,
    val name: String,
    val icon: String,
    val order: Int,
    val services: List<SalonService>
)

data class SalonService(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
    val imageUrl: String,
    val featured: Boolean,
    val bookingEnabled: Boolean,
    val homeServiceAvailable: Boolean
)

enum class DiscountType {
    PERCENTAGE,
    FIXED,
    CUSTOM
}

data class Offer(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String,
    val discount: String,
    val discountType: DiscountType,
    val validFrom: String,
    val validTill: String,
    val terms: String,
    val ctaText: String,
    val ctaAction: String,
    val enabled: Boolean
) {
    val isExpired: Boolean
        get() {
            if (validTill.isBlank()) return false
            return try {
                val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                now > validTill
            } catch (e: Exception) {
                false
            }
        }
}

data class WhyChooseUsItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

data class GalleryImage(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val imageUrl: String,
    val enabled: Boolean,
    val featured: Boolean,
    val order: Int
)

data class Barber(
    val id: String,
    val name: String,
    val expertise: String,
    val imageUrl: String
)

data class Branch(
    val id: String,
    val name: String,
    val tagline: String,
    val address: String,
    val phone: String,
    val whatsapp: String,
    val latitude: Double?,
    val longitude: Double?,
    val googleMapsUrl: String,
    val openingTime: String,
    val closingTime: String,
    val timings: String,
    val enabled: Boolean,
    val isPrimary: Boolean
)

enum class BookingFieldType {
    TEXT,
    PHONE,
    EMAIL,
    DATE,
    TIME,
    SERVICE,
    BRANCH,
    DROPDOWN,
    MULTILINE,
    ADDRESS
}

data class BookingField(
    val id: String,
    val label: String,
    val type: BookingFieldType,
    val placeholder: String,
    val required: Boolean,
    val order: Int,
    val options: List<String>
)

data class BookingConfig(
    val title: String = "",
    val subtitle: String = "",
    val fields: List<BookingField> = emptyList(),
    val enabled: Boolean = true,
    val bookingMessage: String = ""
)

data class Review(
    val id: String,
    val author: String,
    val rating: Double,
    val comment: String,
    val date: String,
    val avatarUrl: String = "",
    val source: String = "",
    val isVerified: Boolean = true
) {
    val isDemo: Boolean
        get() = source.equals("demo", ignoreCase = true) || !isVerified || comment.startsWith("DEMO ONLY", ignoreCase = true)
}

