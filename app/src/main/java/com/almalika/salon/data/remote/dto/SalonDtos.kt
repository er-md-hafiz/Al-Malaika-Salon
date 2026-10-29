package com.almalika.salon.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MenuResponseDto(
    @Json(name = "metadata") val metadata: MetadataDto? = null,
    @Json(name = "salon") val salon: SalonDetailDto? = null,
    @Json(name = "banners") val banners: List<BannerDto>? = null,
    @Json(name = "quickServices") val quickServices: List<MenuItemServiceDto>? = null,
    @Json(name = "popularServices") val popularServices: List<MenuItemServiceDto>? = null,
    @Json(name = "homeServices") val homeServices: List<MenuItemServiceDto>? = null,
    @Json(name = "homeServiceBooking") val homeServiceBooking: HomeServiceBookingDto? = null,
    @Json(name = "priceList") val priceList: List<PriceListItemDto>? = null,
    @Json(name = "offers") val offers: List<RemoteOfferDto>? = null,
    @Json(name = "reviews") val reviews: List<ReviewDto>? = null,
    @Json(name = "barbers") val barbers: List<BarberDto>? = null,
    @Json(name = "galleryItems") val galleryItems: List<RemoteGalleryItemDto>? = null,
    @Json(name = "whyChooseUs") val whyChooseUs: List<WhyChooseUsDto>? = null,
    @Json(name = "profile") val profile: ProfileDto? = null,
    @Json(name = "franchise") val franchise: FranchiseDto? = null,
    @Json(name = "startup") val startup: StartupConfigDto? = null,
    @Json(name = "splashScreen") val splashScreen: SplashScreenDto? = null,
    @Json(name = "onboarding") val onboarding: OnboardingConfigDto? = null,
    @Json(name = "login") val login: LoginConfigDto? = null,
    @Json(name = "register") val register: RegisterConfigDto? = null,
    @Json(name = "forgotPassword") val forgotPassword: ForgotPasswordConfigDto? = null,
    @Json(name = "authentication") val authentication: AuthenticationConfigDto? = null,
    @Json(name = "navigation") val navigation: NavigationConfigDto? = null,
    @Json(name = "home") val home: HomeScreenConfigDto? = null,
    @Json(name = "theme") val theme: ThemeConfigDto? = null,
    @Json(name = "featureFlags") val featureFlags: Map<String, Boolean>? = null,
    @Json(name = "appConfig") val appConfig: AppConfigDto? = null,
    @Json(name = "pages") val pages: Map<String, String>? = null,
    @Json(name = "screens") val screens: List<ScreenSpecDto>? = null,
    @Json(name = "remoteConfig") val remoteConfig: RemoteConfigDto? = null
)

@JsonClass(generateAdapter = true)
data class RemoteConfigDto(
    @Json(name = "url") val url: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "refreshOnLaunch") val refreshOnLaunch: Boolean? = true,
    @Json(name = "refreshIntervalHours") val refreshIntervalHours: Int? = 6,
    @Json(name = "connectTimeoutSeconds") val connectTimeoutSeconds: Int? = 10,
    @Json(name = "readTimeoutSeconds") val readTimeoutSeconds: Int? = 15,
    @Json(name = "useBundledFallback") val useBundledFallback: Boolean? = true,
    @Json(name = "useLastKnownGoodConfigOffline") val useLastKnownGoodConfigOffline: Boolean? = true,
    @Json(name = "cacheOnlyValidatedConfig") val cacheOnlyValidatedConfig: Boolean? = true,
    @Json(name = "applyUpdatesAtomically") val applyUpdatesAtomically: Boolean? = true,
    @Json(name = "checkSchemaVersion") val checkSchemaVersion: Boolean? = true,
    @Json(name = "allowUnknownFields") val allowUnknownFields: Boolean? = true,
    @Json(name = "allowArbitraryCode") val allowArbitraryCode: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class ThemeColorsDto(
    @Json(name = "primary") val primary: String? = null,
    @Json(name = "primaryDark") val primaryDark: String? = null,
    @Json(name = "secondary") val secondary: String? = null,
    @Json(name = "background") val background: String? = null,
    @Json(name = "surface") val surface: String? = null,
    @Json(name = "textPrimary") val textPrimary: String? = null,
    @Json(name = "textSecondary") val textSecondary: String? = null,
    @Json(name = "buttonText") val buttonText: String? = null,
    @Json(name = "divider") val divider: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "success") val success: String? = null,
    @Json(name = "onPrimary") val onPrimary: String? = null,
    @Json(name = "onSurface") val onSurface: String? = null,
    @Json(name = "onError") val onError: String? = null
)

@JsonClass(generateAdapter = true)
data class ThemeConfigDto(
    @Json(name = "themeMode") val themeMode: String? = "dark",
    @Json(name = "darkModeEnabled") val darkModeEnabled: Boolean? = true,
    @Json(name = "colors") val colors: ThemeColorsDto? = null,
    @Json(name = "primaryColor") val primaryColor: String? = null,
    @Json(name = "secondaryColor") val secondaryColor: String? = null,
    @Json(name = "backgroundColor") val backgroundColor: String? = null,
    @Json(name = "surfaceColor") val surfaceColor: String? = null,
    @Json(name = "textColor") val textColor: String? = null,
    @Json(name = "cornerRadiusDp") val cornerRadiusDp: Int? = null,
    @Json(name = "buttonCornerRadiusDp") val buttonCornerRadiusDp: Int? = null,
    @Json(name = "useSystemStatusBarIcons") val useSystemStatusBarIcons: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class StartupConfigDto(
    @Json(name = "splashScreenId") val splashScreenId: String? = "splash",
    @Json(name = "onboardingScreenId") val onboardingScreenId: String? = "onboarding",
    @Json(name = "loginScreenId") val loginScreenId: String? = "login",
    @Json(name = "homeScreenId") val homeScreenId: String? = "home",
    @Json(name = "defaultDestination") val defaultDestination: String? = "home",
    @Json(name = "requireLogin") val requireLogin: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class SplashScreenDto(
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "backgroundColor") val backgroundColor: String? = "#121212",
    @Json(name = "textColor") val textColor: String? = "#FFFFFF",
    @Json(name = "accentColor") val accentColor: String? = "#D4AF37",
    @Json(name = "minimumDurationMs") val minimumDurationMs: Long? = 1500L,
    @Json(name = "animationType") val animationType: String? = "fade"
)

@JsonClass(generateAdapter = true)
data class OnboardingConfigDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "showSkip") val showSkip: Boolean? = null,
    @Json(name = "allowSkip") val allowSkip: Boolean? = null,
    @Json(name = "showOnlyOnFirstLaunch") val showOnlyOnFirstLaunch: Boolean? = null,
    @Json(name = "nextButtonText") val nextButtonText: String? = null,
    @Json(name = "previousButtonText") val previousButtonText: String? = null,
    @Json(name = "skipButtonText") val skipButtonText: String? = null,
    @Json(name = "finishButtonText") val finishButtonText: String? = null,
    @Json(name = "pages") val pages: List<OnboardingPageDto>? = null
)

@JsonClass(generateAdapter = true)
data class OnboardingPageDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "order") val order: Int? = 0,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "illustrationUrl") val illustrationUrl: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "nextLabel") val nextLabel: String? = null,
    @Json(name = "skipLabel") val skipLabel: String? = null,
    @Json(name = "getStartedLabel") val getStartedLabel: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginConfigDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "authMode") val authMode: String? = null,
    @Json(name = "allowGuest") val allowGuest: Boolean? = true,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "backgroundColor") val backgroundColor: String? = null,
    @Json(name = "textColor") val textColor: String? = null,
    @Json(name = "accentColor") val accentColor: String? = null,
    @Json(name = "showEmailLogin") val showEmailLogin: Boolean? = true,
    @Json(name = "showPasswordLogin") val showPasswordLogin: Boolean? = false,
    @Json(name = "showPhoneLogin") val showPhoneLogin: Boolean? = false,
    @Json(name = "showRegister") val showRegister: Boolean? = true,
    @Json(name = "showForgotPassword") val showForgotPassword: Boolean? = true,
    @Json(name = "showGuestButton") val showGuestButton: Boolean? = true,
    @Json(name = "phonePlaceholder") val phonePlaceholder: String? = null,
    @Json(name = "continueAsGuestLabel") val continueAsGuestLabel: String? = null,
    @Json(name = "guestButtonText") val guestButtonText: String? = null,
    @Json(name = "loginButtonLabel") val loginButtonLabel: String? = null,
    @Json(name = "loginButtonText") val loginButtonText: String? = null,
    @Json(name = "emailLinkButtonText") val emailLinkButtonText: String? = null,
    @Json(name = "registerButtonText") val registerButtonText: String? = null,
    @Json(name = "forgotPasswordButtonText") val forgotPasswordButtonText: String? = null,
    @Json(name = "privacyPolicyUrl") val privacyPolicyUrl: String? = null,
    @Json(name = "termsUrl") val termsUrl: String? = null,
    @Json(name = "registerDestination") val registerDestination: String? = null,
    @Json(name = "forgotPasswordDestination") val forgotPasswordDestination: String? = null,
    @Json(name = "successDestination") val successDestination: String? = null
)

@JsonClass(generateAdapter = true)
data class RegisterConfigDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "showFullName") val showFullName: Boolean? = true,
    @Json(name = "showEmail") val showEmail: Boolean? = true,
    @Json(name = "showPassword") val showPassword: Boolean? = true,
    @Json(name = "showConfirmPassword") val showConfirmPassword: Boolean? = true,
    @Json(name = "requireTermsAcceptance") val requireTermsAcceptance: Boolean? = true,
    @Json(name = "sendEmailVerification") val sendEmailVerification: Boolean? = true,
    @Json(name = "termsUrl") val termsUrl: String? = null,
    @Json(name = "privacyPolicyUrl") val privacyPolicyUrl: String? = null,
    @Json(name = "loginDestination") val loginDestination: String? = null,
    @Json(name = "successDestination") val successDestination: String? = null
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordConfigDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "emailRequired") val emailRequired: Boolean? = true,
    @Json(name = "loginDestination") val loginDestination: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthenticationConfigDto(
    @Json(name = "provider") val provider: String? = "firebase",
    @Json(name = "emailPasswordEnabled") val emailPasswordEnabled: Boolean? = true,
    @Json(name = "anonymousEnabled") val anonymousEnabled: Boolean? = true,
    @Json(name = "phoneAuthEnabled") val phoneAuthEnabled: Boolean? = false,
    @Json(name = "guestAccountLinkingEnabled") val guestAccountLinkingEnabled: Boolean? = true,
    @Json(name = "persistSession") val persistSession: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class NavigationConfigDto(
    @Json(name = "drawerEnabled") val drawerEnabled: Boolean? = true,
    @Json(name = "bottomNavigationEnabled") val bottomNavigationEnabled: Boolean? = true,
    @Json(name = "bottomNavigationMaxItems") val bottomNavigationMaxItems: Int? = 5,
    @Json(name = "drawerItems") val drawerItems: List<NavDestinationDto>? = null,
    @Json(name = "bottomItems") val bottomItems: List<NavDestinationDto>? = null
)

@JsonClass(generateAdapter = true)
data class NavDestinationDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "selectedIcon") val selectedIcon: String? = null,
    @Json(name = "route") val route: String? = null,
    @Json(name = "destination") val destination: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "order") val order: Int? = 0,
    @Json(name = "badge") val badge: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeScreenConfigDto(
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "screenTitle") val screenTitle: String? = null,
    @Json(name = "sections") val sections: List<HomeSectionConfigDto>? = null
)

@JsonClass(generateAdapter = true)
data class HomeSectionActionDto(
    @Json(name = "type") val type: String? = null,
    @Json(name = "destination") val destination: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeSectionConfigDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "actionText") val actionText: String? = null,
    @Json(name = "actionRoute") val actionRoute: String? = null,
    @Json(name = "sourceUrl") val sourceUrl: String? = null,
    @Json(name = "itemsToShow") val itemsToShow: Int? = null,
    @Json(name = "maxItems") val maxItems: Int? = null,
    @Json(name = "dataSource") val dataSource: String? = null,
    @Json(name = "viewAllDestination") val viewAllDestination: String? = null,
    @Json(name = "action") val action: HomeSectionActionDto? = null,
    @Json(name = "itemAction") val itemAction: HomeSectionActionDto? = null,
    @Json(name = "autoSlideIntervalMs") val autoSlideIntervalMs: Long? = 4000L,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "order") val order: Int? = 0
)

@JsonClass(generateAdapter = true)
data class MetadataDto(
    @Json(name = "schemaVersion") val schemaVersion: String? = null,
    @Json(name = "uiConfigVersion") val uiConfigVersion: Int? = null,
    @Json(name = "lastUpdated") val lastUpdated: String? = null,
    @Json(name = "locale") val locale: String? = null,
    @Json(name = "currencyCode") val currencyCode: String? = null
)

@JsonClass(generateAdapter = true)
data class AppConfigDto(
    @Json(name = "appName") val appName: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "defaultLanguage") val defaultLanguage: String? = null,
    @Json(name = "minimumSupportedConfigVersion") val minimumSupportedConfigVersion: Int? = null,
    @Json(name = "maintenanceMode") val maintenanceMode: Boolean? = false,
    @Json(name = "maintenanceMessage") val maintenanceMessage: String? = null,
    @Json(name = "forceUpdate") val forceUpdate: Boolean? = false,
    @Json(name = "minimumAppVersion") val minimumAppVersion: String? = null,
    @Json(name = "showInternetStatus") val showInternetStatus: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class SalonDetailDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "about") val about: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "mobileNumber") val mobileNumber: String? = null,
    @Json(name = "mobileNumberDial") val mobileNumberDial: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "whatsappUrl") val whatsappUrl: String? = null,
    @Json(name = "rating") val rating: Double? = null,
    @Json(name = "totalReviews") val totalReviews: Int? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "openingHours") val openingHours: String? = null,
    @Json(name = "workingDays") val workingDays: String? = null,
    @Json(name = "mapsEmbedUrl") val mapsEmbedUrl: String? = null,
    @Json(name = "mapsSearchUrl") val mapsSearchUrl: String? = null,
    @Json(name = "playStoreUrl") val playStoreUrl: String? = null,
    @Json(name = "social") val social: SocialDto? = null
)

@JsonClass(generateAdapter = true)
data class SocialDto(
    @Json(name = "instagram") val instagram: String? = null,
    @Json(name = "facebook") val facebook: String? = null
)

@JsonClass(generateAdapter = true)
data class BannerDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "serviceType") val serviceType: String? = null,
    @Json(name = "isAvailable") val isAvailable: Boolean? = true,
    @Json(name = "action") val action: HomeSectionActionDto? = null
)

@JsonClass(generateAdapter = true)
data class MenuItemServiceDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "serviceType") val serviceType: String? = null,
    @Json(name = "isAvailable") val isAvailable: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class HomeServiceBookingDto(
    @Json(name = "enabled") val enabled: Boolean? = false,
    @Json(name = "minimumBookingAmount") val minimumBookingAmount: Double? = null,
    @Json(name = "serviceCharge") val serviceCharge: Double? = null,
    @Json(name = "freeServiceChargeAbove") val freeServiceChargeAbove: Double? = null,
    @Json(name = "advanceBookingHours") val advanceBookingHours: Int? = null,
    @Json(name = "bookingMessage") val bookingMessage: String? = null,
    @Json(name = "availableDays") val availableDays: List<String>? = null,
    @Json(name = "timeSlots") val timeSlots: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PriceListItemDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteOfferDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "discount") val discount: String? = null,
    @Json(name = "discountType") val discountType: String? = null,
    @Json(name = "validFrom") val validFrom: String? = null,
    @Json(name = "validTill") val validTill: String? = null,
    @Json(name = "terms") val terms: String? = null,
    @Json(name = "ctaText") val ctaText: String? = null,
    @Json(name = "ctaAction") val ctaAction: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class ReviewDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "rating") val rating: Double? = null,
    @Json(name = "comment") val comment: String? = null,
    @Json(name = "reviewText") val reviewText: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "avatarUrl") val avatarUrl: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "isVerified") val isVerified: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class BarberDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "expertise") val expertise: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteGalleryItemDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "aspectRatio") val aspectRatio: Double? = null
)

@JsonClass(generateAdapter = true)
data class WhyChooseUsDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ProfileDto(
    @Json(name = "appName") val appName: String? = null,
    @Json(name = "appVersion") val appVersion: String? = null,
    @Json(name = "shareMessage") val shareMessage: String? = null,
    @Json(name = "supportEmail") val supportEmail: String? = null,
    @Json(name = "supportPhone") val supportPhone: String? = null,
    @Json(name = "privacyPolicyUrl") val privacyPolicyUrl: String? = null,
    @Json(name = "termsUrl") val termsUrl: String? = null,
    @Json(name = "aboutUs") val aboutUs: String? = null,
    @Json(name = "darkModeEnabled") val darkModeEnabled: Boolean? = true,
    @Json(name = "notificationEnabled") val notificationEnabled: Boolean? = true,
    @Json(name = "menuItems") val menuItems: List<ProfileMenuItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class ProfileMenuItemDto(
    @Json(name = "id") val id: Any? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "icon") val icon: String? = null
)

@JsonClass(generateAdapter = true)
data class ScreenSpecDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "enabled") val enabled: Boolean? = true,
    @Json(name = "showInDrawer") val showInDrawer: Boolean? = false,
    @Json(name = "showInBottomNavigation") val showInBottomNavigation: Boolean? = false,
    @Json(name = "requiresLogin") val requiresLogin: Boolean? = false,
    @Json(name = "urlKey") val urlKey: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null
)

@JsonClass(generateAdapter = true)
data class FranchiseDto(
    @Json(name = "enabled") val enabled: Boolean? = false,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "investment") val investment: String? = null,
    @Json(name = "franchiseFee") val franchiseFee: String? = null,
    @Json(name = "shopArea") val shopArea: String? = null,
    @Json(name = "preferredLocation") val preferredLocation: String? = null,
    @Json(name = "expectedROI") val expectedROI: String? = null,
    @Json(name = "support") val support: List<String>? = null,
    @Json(name = "requirements") val requirements: List<String>? = null,
    @Json(name = "benefits") val benefits: List<String>? = null,
    @Json(name = "contactPerson") val contactPerson: String? = null,
    @Json(name = "contactNumber") val contactNumber: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "whatsApp") val whatsApp: String? = null,
    @Json(name = "website") val website: String? = null,
    @Json(name = "bannerImage") val bannerImage: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "ctaUrl") val ctaUrl: String? = null,
    @Json(name = "ctaText") val ctaText: String? = null
)
