# Al-Malaika Life Style Salon - Fully Dynamic Android App Platform

A production-ready, backend/configuration-driven Android application platform built for **Al-Malaika Life Style Salon** (Alfalah Colony, Pugmil, Hazaribagh, Jharkhand 825319).

The application is engineered as a **reusable salon framework**. No salon-specific content (services, prices, images, offers, timings, booking fields, WhatsApp numbers, branch locations, social links, legal URLs, or menus) is hardcoded into the APK. All elements are controlled dynamically via remote JSON / REST APIs and cached offline with Room.

---

## 🏗️ Architecture & Technology Stack

* **Language**: Kotlin
* **UI**: Jetpack Compose with Material 3 (luxury gold and charcoal aesthetic)
* **Architecture**: Clean Architecture (Data → Domain → Presentation) + MVVM + Unidirectional Data Flow
* **Dependency Injection**: Hilt (Dagger)
* **Networking**: Retrofit 2 + OkHttp 4 + Moshi
* **Local Persistence**: Room Database (with atomic replacement and reactive Flow streams)
* **Asynchronous / Reactive**: Kotlin Coroutines & Flow / StateFlow
* **Image Loading & Caching**: Coil Compose
* **Navigation**: Navigation Compose
* **Platform Integrations**:
  * WhatsApp Direct Deep-linking & Dynamic Message Templating
  * Google Maps Geolocation & Directions Intent
  * Android Sharesheet (`ACTION_SEND`)
  * Google Play In-App Review API (`ReviewManager`)
  * AndroidX WorkManager for background configuration sync
  * AndroidX DataStore

---

## 📁 Repository Structure

```text
app/
├── src/main/
│   ├── assets/config/             <-- Bundled fallback JSONs for first-run offline resilience
│   │   ├── menu_fallback.json
│   │   ├── home_fallback.json
│   │   ├── services_fallback.json
│   │   ├── offers_fallback.json
│   │   ├── gallery_fallback.json
│   │   ├── locations_fallback.json
│   │   ├── booking_fallback.json
│   │   └── home_service_fallback.json
│   └── java/com/almalika/salon/
│       ├── core/                  <-- UiState, AppAction, ActionExecutor
│       ├── data/
│       │   ├── local/             <-- Room Database, DAO, Entities, AssetConfigReader
│       │   ├── mapper/            <-- DTO to Domain mappers
│       │   ├── remote/            <-- Retrofit API, Moshi DTOs
│       │   ├── repository/        <-- SalonRepositoryImpl
│       │   └── sync/              <-- ConfigSyncWorker
│       ├── di/                    <-- Hilt Modules (Network, Database, Repository)
│       ├── domain/
│       │   ├── model/             <-- Pure Kotlin domain models
│       │   ├── repository/        <-- SalonRepository interface
│       │   └── usecase/           <-- GetHomeConfig, GetServices, GetOffers, etc.
│       └── presentation/
│           ├── common/            <-- Shimmer, ErrorRetryView, EmptyView
│           ├── home/              <-- DynamicSectionRenderer, HomeScreen, HomeViewModel
│           ├── services/          <-- ServicesScreen, ServicesViewModel
│           ├── offers/            <-- OffersScreen, OffersViewModel
│           ├── gallery/           <-- GalleryScreen, GalleryViewModel
│           ├── locations/         <-- LocationsScreen, LocationsViewModel
│           ├── booking/           <-- BookingScreen, BookingViewModel
│           ├── homeservice/       <-- HomeServiceScreen, HomeServiceViewModel
│           ├── about/             <-- AboutScreen
│           ├── contact/           <-- ContactScreen
│           ├── settings/          <-- SettingsScreen
│           ├── navigation/        <-- IconResolver
│           ├── theme/             <-- Luxury Material 3 Theme
│           └── MainActivity.kt
backend/
├── config/                        <-- Sample production configuration files
└── images/                        <-- Image CDN folder structure (banners, services, gallery)
```

---

## 🚀 Key Features & Dynamic Workflows

### 1. Zero-APK Content Changes
* **Change Service Price**: Edit `services.json` on the backend -> Android syncs -> New price displays instantly.
* **Upload Gallery Image**: Add image object to `gallery.json` -> Displayed in both home preview and gallery grid.
* **Disable Offers**: Set `"offers": { "enabled": false }` in `menu.json` -> Automatically removed from Bottom Navigation, Drawer, and Home screen.
* **Change WhatsApp Number**: Update `"whatsapp": { "number": "91..." }` -> All booking and contact buttons instantly direct to the new number.
* **Add Branch**: Add branch entry to `locations.json` -> Automatically appears in locations list and booking branch dropdown.

### 2. Offline-First Resilience
1. Loads cached Room database on launch immediately.
2. If initial launch with no network, loads bundled `assets/config/*_fallback.json`.
3. Background sync checks `configVersion` from the server. If newer, atomically updates local cache.

### 3. Dynamic Booking & WhatsApp Integration
The booking form fields (e.g. name, phone, branch, service, date, time, address) are generated from JSON. On submission, the app dynamically replaces placeholders in the template:
```text
Hello {salonName}, I would like to book an appointment.
Name: {name}
Phone: {phone}
Branch: {branch}
Service: {service}
Date: {date}
Time: {time}
Message: {message}
```
and launches WhatsApp with the pre-filled message.

---

## 🛠️ Environment Configuration & Base URL

To configure your production backend base URL, modify `NetworkModule.kt`:
```kotlin
private const val BASE_URL = "https://api.almalaikasalon.com/"
```
Or inject via BuildConfig or a secure remote configuration endpoint.
