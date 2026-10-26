package com.almalika.salon.core.action

sealed interface AppAction {
    data object Home : AppAction
    data object Services : AppAction
    data object Offers : AppAction
    data object Booking : AppAction
    data object HomeService : AppAction
    data object Gallery : AppAction
    data object Locations : AppAction
    data object About : AppAction
    data object Contact : AppAction
    data object Settings : AppAction

    data class SelectService(val serviceId: String) : AppAction
    data class SelectOffer(val offerId: String) : AppAction
    data class SelectBranch(val branchId: String) : AppAction
    data class OpenGalleryImage(val imageId: String) : AppAction

    data class Call(val phoneNumber: String) : AppAction
    data class WhatsApp(val phoneNumber: String, val text: String = "") : AppAction
    data class OpenMap(val queryOrUrl: String, val lat: Double? = null, val lng: Double? = null) : AppAction
    data class ShareApp(val message: String, val url: String) : AppAction
    data object RateApp : AppAction
    data object Terms : AppAction
    data object Privacy : AppAction
    data class OpenUrl(val url: String, val title: String = "") : AppAction
}
