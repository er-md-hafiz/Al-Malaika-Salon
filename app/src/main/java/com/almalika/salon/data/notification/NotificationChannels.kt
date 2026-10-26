package com.almalika.salon.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

object NotificationChannels {

    const val CHANNEL_APPOINTMENTS = "salon_appointments"
    const val CHANNEL_OFFERS = "salon_offers"
    const val CHANNEL_UPDATES = "salon_updates"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val appointmentsChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Appointment Confirmations & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications regarding your salon appointments and booking updates."
                enableLights(true)
                lightColor = Color.parseColor("#D4AF37") // Gold primary
                enableVibration(true)
            }

            val offersChannel = NotificationChannel(
                CHANNEL_OFFERS,
                "Special Offers & Packages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Exclusive discounts, package specials, and promotional announcements."
                enableLights(true)
                lightColor = Color.parseColor("#D4AF37")
            }

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "Salon Updates & Notices",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Operating hours, styling trends, and salon notices."
            }

            notificationManager.createNotificationChannels(
                listOf(appointmentsChannel, offersChannel, updatesChannel)
            )
        }
    }
}
