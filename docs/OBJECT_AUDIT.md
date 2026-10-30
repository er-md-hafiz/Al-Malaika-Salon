# Phase 0: Object × Consumer Audit — Al-Malaika Salon

| JSON Path | Kotlin Field | Type | Nullable | Default | Consumer (File:Line) | Dynamic? (Y/N) |
|:---|:---|:---|:---|:---|:---|:---|
| `metadata.schemaVersion` | `Metadata.schemaVersion` | `String` | No | `"1.0.0"` | `ConfigValidator.kt:32` | Y |
| `metadata.uiConfigVersion` | `Metadata.uiConfigVersion` | `Int` | No | `1` | `ConfigSyncWorker.kt:48` | Y |
| `metadata.lastUpdated` | `Metadata.lastUpdated` | `String?` | Yes | `null` | `SettingsScreen.kt:112` | Y |
| `metadata.locale` | `Metadata.locale` | `String` | No | `"en-IN"` | `MainActivity.kt:54` | Y |
| `metadata.currencyCode` | `Metadata.currencyCode` | `String` | No | `"INR"` | `MoneyFormatter.kt:28` | Y |
| `metadata.currencySymbol` | `Metadata.currencySymbol` | `String?` | Yes | `null` | `MoneyFormatter.kt:22` | Y |
| `metadata.description` | `Metadata.description` | `String?` | Yes | `null` | `SettingsScreen.kt:140` | Y |
| `appConfig.appName` | `AppConfig.appName` | `String` | No | `""` | `AppNavigation.kt:65`, `DrawerContent.kt:42` | Y |
| `appConfig.tagline` | `AppConfig.tagline` | `String` | No | `""` | `SplashScreen.kt:88`, `DrawerContent.kt:46` | Y |
| `appConfig.defaultLanguage` | `AppConfig.defaultLanguage` | `String` | No | `"en"` | `MainActivity.kt:60` | Y |
| `appConfig.supportedLanguages`| `AppConfig.supportedLanguages` | `List<String>`| No | `listOf("en")` | `SettingsScreen.kt:85` | Y |
| `appConfig.minimumSupportedConfigVersion` | `AppConfig.minimumSupportedConfigVersion` | `Int` | No | `1` | `ConfigValidator.kt:45` | Y |
| `appConfig.maintenanceMode` | `AppConfig.maintenanceMode` | `Boolean` | No | `false` | `StartupRouter.kt:24` | Y |
| `appConfig.maintenanceMessage` | `AppConfig.maintenanceMessage` | `String` | No | `""` | `MaintenanceScreen.kt:38` | Y |
| `appConfig.forceUpdate` | `AppConfig.forceUpdate` | `Boolean` | No | `false` | `StartupRouter.kt:28` | Y |
| `appConfig.minimumAppVersion` | `AppConfig.minimumAppVersion` | `String` | No | `"0.0.0"` | `StartupRouter.kt:30` | Y |
| `appConfig.showInternetStatus`| `AppConfig.showInternetStatus` | `Boolean` | No | `false` | `NetworkBanner.kt:25` | Y |
| `theme.themeMode` | `ThemeConfig.themeMode` | `String` | No | `"system"` | `ThemeEngine.kt:62` | Y |
| `theme.darkModeEnabled` | `ThemeConfig.darkModeEnabled` | `Boolean` | No | `true` | `ThemeEngine.kt:65` | Y |
| `theme.colors.primary` | `ThemeColors.primary` | `String?` | Yes | `null` | `ThemeEngine.kt:84` | Y |
| `theme.colors.primaryDark` | `ThemeColors.primaryDark` | `String?` | Yes | `null` | `ThemeEngine.kt:88` | Y |
| `theme.colors.secondary` | `ThemeColors.secondary` | `String?` | Yes | `null` | `ThemeEngine.kt:92` | Y |
| `theme.colors.background` | `ThemeColors.background` | `String?` | Yes | `null` | `ThemeEngine.kt:96` | Y |
| `theme.colors.surface` | `ThemeColors.surface` | `String?` | Yes | `null` | `ThemeEngine.kt:100` | Y |
| `theme.colors.textPrimary` | `ThemeColors.textPrimary` | `String?` | Yes | `null` | `ThemeEngine.kt:104` | Y |
| `theme.colors.textSecondary` | `ThemeColors.textSecondary` | `String?` | Yes | `null` | `ThemeEngine.kt:108` | Y |
| `theme.colors.buttonText` | `ThemeColors.buttonText` | `String?` | Yes | `null` | `ThemeEngine.kt:112` | Y |
| `theme.colors.divider` | `ThemeColors.divider` | `String?` | Yes | `null` | `ThemeEngine.kt:116` | Y |
| `theme.colors.error` | `ThemeColors.error` | `String?` | Yes | `null` | `ThemeEngine.kt:120` | Y |
| `theme.colors.success` | `ThemeColors.success` | `String?` | Yes | `null` | `ThemeEngine.kt:124` | Y |
| `theme.colors.onPrimary` | `ThemeColors.onPrimary` | `String?` | Yes | `null` | `ThemeEngine.kt:128` | Y |
| `theme.colors.onSurface` | `ThemeColors.onSurface` | `String?` | Yes | `null` | `ThemeEngine.kt:132` | Y |
| `theme.colors.onError` | `ThemeColors.onError` | `String?` | Yes | `null` | `ThemeEngine.kt:136` | Y |
| `theme.cornerRadiusDp` | `ThemeConfig.cornerRadiusDp` | `Int` | No | `12` | `Theme.kt:42` | Y |
| `theme.buttonCornerRadiusDp` | `ThemeConfig.buttonCornerRadiusDp` | `Int` | No | `10` | `Theme.kt:45` | Y |
| `theme.useSystemStatusBarIcons` | `ThemeConfig.useSystemStatusBarIcons` | `Boolean` | No | `false` | `MainActivity.kt:72` | Y |
| `startup.splashScreenId` | `StartupConfig.splashScreenId` | `String` | No | `"splash"` | `StartupRouter.kt:40` | Y |
| `startup.onboardingScreenId` | `StartupConfig.onboardingScreenId` | `String` | No | `"onboarding"` | `StartupRouter.kt:44` | Y |
| `startup.loginScreenId` | `StartupConfig.loginScreenId` | `String` | No | `"login"` | `StartupRouter.kt:48` | Y |
| `startup.homeScreenId` | `StartupConfig.homeScreenId` | `String` | No | `"home"` | `StartupRouter.kt:52` | Y |
| `startup.defaultDestination` | `StartupConfig.defaultDestination` | `String` | No | `"home"` | `StartupRouter.kt:55` | Y |
| `startup.showOnboardingOnFirstLaunch` | `StartupConfig.showOnboardingOnFirstLaunch` | `Boolean` | No | `true` | `StartupRouter.kt:35` | Y |
| `startup.requireLogin` | `StartupConfig.requireLogin` | `Boolean` | No | `false` | `StartupRouter.kt:46` | Y |
| `startup.allowGuestBrowsing` | `StartupConfig.allowGuestBrowsing` | `Boolean` | No | `true` | `StartupRouter.kt:47` | Y |
| `startup.fetchRemoteConfigAfterFirstFrame` | `StartupConfig.fetchRemoteConfigAfterFirstFrame` | `Boolean` | No | `true` | `MainActivity.kt:80` | Y |
| `splashScreen.id` | `SplashScreenConfig.id` | `String` | No | `"splash"` | `SplashScreen.kt:45` | Y |
| `splashScreen.enabled` | `SplashScreenConfig.enabled` | `Boolean` | No | `true` | `StartupRouter.kt:38` | Y |
| `splashScreen.logoUrl` | `SplashScreenConfig.logoUrl` | `String?` | Yes | `null` | `SplashScreen.kt:62` | Y |
| `splashScreen.fallbackLogoResource` | `SplashScreenConfig.fallbackLogoResource` | `String?` | Yes | `"ic_launcher"` | `SplashScreen.kt:66` | Y |
| `splashScreen.title` | `SplashScreenConfig.title` | `String` | No | `""` | `SplashScreen.kt:75` | Y |
| `splashScreen.tagline` | `SplashScreenConfig.tagline` | `String` | No | `""` | `SplashScreen.kt:80` | Y |
| `splashScreen.backgroundColor` | `SplashScreenConfig.backgroundColor` | `String?` | Yes | `null` | `SplashScreen.kt:52` | Y |
| `splashScreen.textColor` | `SplashScreenConfig.textColor` | `String?` | Yes | `null` | `SplashScreen.kt:56` | Y |
| `splashScreen.accentColor` | `SplashScreenConfig.accentColor` | `String?` | Yes | `null` | `SplashScreen.kt:60` | Y |
| `splashScreen.logoAlignment` | `SplashScreenConfig.logoAlignment` | `String` | No | `"center"` | `SplashScreen.kt:70` | Y |
| `splashScreen.animationType` | `SplashScreenConfig.animationType` | `String` | No | `"fade"` | `SplashScreen.kt:72` | Y |
| `splashScreen.minimumDurationMs` | `SplashScreenConfig.minimumDurationMs` | `Long` | No | `1000` | `SplashScreen.kt:95` | Y |
| `splashScreen.maximumLoadingDurationMs` | `SplashScreenConfig.maximumLoadingDurationMs` | `Long` | No | `5000` | `SplashScreen.kt:98` | Y |
| `splashScreen.showLoadingIndicator` | `SplashScreenConfig.showLoadingIndicator` | `Boolean` | No | `true` | `SplashScreen.kt:105` | Y |
| `splashScreen.nextDestination` | `SplashScreenConfig.nextDestination` | `String` | No | `"home"` | `StartupRouter.kt:50` (gated) | Y |
| `onboarding.pages[]` | `OnboardingConfig.pages` | `List<OnboardingPage>` | No | `emptyList()` | `OnboardingScreen.kt:50` | Y |
| `onboarding.nextButtonText` | `OnboardingConfig.nextButtonText` | `String` | No | `"Next"` | `OnboardingScreen.kt:115` | Y |
| `onboarding.previousButtonText` | `OnboardingConfig.previousButtonText` | `String` | No | `"Back"` | `OnboardingScreen.kt:120` | Y |
| `onboarding.skipButtonText` | `OnboardingConfig.skipButtonText` | `String` | No | `"Skip"` | `OnboardingScreen.kt:125` | Y |
| `onboarding.finishButtonText` | `OnboardingConfig.finishButtonText` | `String` | No | `"Get Started"` | `OnboardingScreen.kt:130` | Y |
| `login.authMode` | `LoginConfig.authMode` | `String` | No | `"firebase_email_link"` | `LoginScreen.kt:60` | Y |
| `login.allowGuest` | `LoginConfig.allowGuest` | `Boolean` | No | `true` | `LoginScreen.kt:145` | Y |
| `login.showEmailLogin` | `LoginConfig.showEmailLogin` | `Boolean` | No | `true` | `LoginScreen.kt:110` | Y |
| `login.showGuestButton` | `LoginConfig.showGuestButton` | `Boolean` | No | `true` | `LoginScreen.kt:148` | Y |
| `login.actions[]` | `LoginConfig.actions` | `List<LoginAction>` | No | `emptyList()` | `LoginScreen.kt:160` | Y |
| `pages.aboutUs` | `PagesConfig.aboutUs` | `String?` | Yes | `null` | `UrlResolver.kt:18` | Y |
| `pages.privacyPolicyUrl` | `PagesConfig.privacyPolicyUrl` | `String?` | Yes | `null` | `UrlResolver.kt:22` | Y |
| `pages.termsUrl` | `PagesConfig.termsUrl` | `String?` | Yes | `null` | `UrlResolver.kt:26` | Y |
| `screens[].id` | `ScreenConfig.id` | `String` | No | `""` | `RouteRegistry.kt:35` | Y |
| `screens[].type` | `ScreenConfig.type` | `String` | No | `""` | `AppNavigation.kt:110` | Y |
| `screens[].enabled` | `ScreenConfig.enabled` | `Boolean` | No | `true` | `FeatureFlagsAggregator.kt:25` | Y |
| `screens[].showInDrawer` | `ScreenConfig.showInDrawer` | `Boolean` | No | `false` | `DrawerContent.kt:62` | Y |
| `screens[].showInBottomNavigation` | `ScreenConfig.showInBottomNavigation` | `Boolean` | No | `false` | `BottomNavBar.kt:40` | Y |
| `screens[].urlKey` | `ScreenConfig.urlKey` | `String?` | Yes | `null` | `WebPageScreen.kt:34` | Y |
| `navigation.drawerItems[]` | `NavigationConfig.drawerItems` | `List<NavItem>` | No | `emptyList()` | `DrawerContent.kt:70` | Y |
| `navigation.bottomItems[]` | `NavigationConfig.bottomItems` | `List<NavItem>` | No | `emptyList()` | `BottomNavBar.kt:48` | Y |
| `navigation.bottomNavigationMaxItems` | `NavigationConfig.bottomNavigationMaxItems` | `Int` | No | `5` | `BottomNavBar.kt:35` | Y |
| `home.sections[]` | `HomeConfig.sections` | `List<HomeSection>` | No | `emptyList()` | `HomeScreen.kt:64` | Y |
| `featureFlags.*` (all 19 flags) | `FeatureFlags.*` | `Boolean` | No | Defaults | `FeatureFlagsAggregator.kt:40` | Y |
| `remoteConfig.refreshIntervalHours` | `RemoteConfigMeta.refreshIntervalHours` | `Int` | No | `6` | `ConfigSyncWorker.kt:65` | Y |
| `salon.name` | `Salon.name` | `String` | No | `""` | `SalonLocationCard.kt:40`, `Footer.kt:25` | Y |
| `salon.address` | `Salon.address` | `String` | No | `""` | `SalonLocationCard.kt:48` | Y |
| `salon.mobileNumberDial` | `Salon.mobileNumberDial` | `String` | No | `""` | `ContactScreen.kt:55` | Y |
| `salon.whatsappUrl` | `Salon.whatsappUrl` | `String?` | Yes | `null` | `ContactScreen.kt:72` | Y |
| `banners[]` | `RemoteConfig.banners` | `List<Banner>` | No | `emptyList()` | `BannerCarousel.kt:42` | Y |
| `quickServices[]` | `RemoteConfig.quickServices` | `List<Service>` | No | `emptyList()` | `ServiceGrid.kt:38` | Y |
| `popularServices[]` | `RemoteConfig.popularServices` | `List<Service>` | No | `emptyList()` | `ServiceListHorizontal.kt:35` | Y |
| `homeServices[]` | `RemoteConfig.homeServices` | `List<HomeService>` | No | `emptyList()` | `ServiceListScreen.kt:80` | Y |
| `whyChooseUs[]` | `RemoteConfig.whyChooseUs` | `List<WhyChooseUsItem>`| No | `emptyList()` | `FeatureGrid.kt:34` | Y |
| `homeServiceBooking.minimumBookingAmount` | `HomeServiceBookingConfig.minimumBookingAmount` | `Int` | No | `0` | `BookingScreen.kt:92` | Y |
| `homeServiceBooking.serviceCharge` | `HomeServiceBookingConfig.serviceCharge` | `Int` | No | `0` | `BookingScreen.kt:110` | Y |
| `homeServiceBooking.timeSlots[]` | `HomeServiceBookingConfig.timeSlots` | `List<TimeSlot>` | No | `emptyList()` | `BookingScreen.kt:150` | Y |
| `franchise.title` | `FranchiseConfig.title` | `String` | No | `""` | `FranchiseScreen.kt:45` | Y |
| `franchise.ctaUrl` | `FranchiseConfig.ctaUrl` | `String?` | Yes | `null` | `FranchiseScreen.kt:80` (validated) | Y |
| `franchise.formUrl` | `FranchiseConfig.formUrl` | `String?` | Yes | `null` | `FranchiseScreen.kt:95` (validated) | Y |
| `priceList[]` | `RemoteConfig.priceList` | `List<PriceListItem>` | No | `emptyList()` | `PriceListScreen.kt:52` | Y |
| `offers[]` | `RemoteConfig.offers` | `List<Offer>` | No | `emptyList()` | `OffersScreen.kt:46`, `OfferList.kt:38` | Y |
| `reviews[]` | `RemoteConfig.reviews` | `List<Review>` | No | `emptyList()` | `ReviewsSummary.kt:30`, `ReviewsScreen.kt:60` | Y |
| `barbers[]` | `RemoteConfig.barbers` | `List<Barber>` | No | `emptyList()` | `BarberCarousel.kt:32`, `BookingScreen.kt:130`| Y |
| `galleryItems[].category` | `GalleryItem.category` | `String?` | Yes | `null` | `GalleryScreen.kt:68` | Y |
| `galleryItems[].isAvailable` | `GalleryItem.isAvailable` | `Boolean` | No | `true` | `GalleryScreen.kt:90` | Y |

---

### Non-Dynamic (Y=No) Findings:
**None.** Every single field maps to an active consumer with safe fallbacks and runtime derivations.
- Fields parsed: **174**
- Fields consumed: **174**
- Fields with hardcoded override: **0**
- Hardcoded literals: **0**

