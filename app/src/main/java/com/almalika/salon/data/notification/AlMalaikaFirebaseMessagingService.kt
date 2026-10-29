package com.almalika.salon.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.almalika.salon.R
import com.almalika.salon.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlMalaikaFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val EXTRA_TARGET_ROUTE = "extra_target_route"
        const val EXTRA_CAMPAIGN = "extra_campaign"
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    FirebaseServiceEntryPoint::class.java
                )
                entryPoint.getFcmTokenManager().saveToken(token)
                entryPoint.getCrashlyticsHelper().log("FCM Token refreshed successfully")
            } catch (e: Exception) {
                // Prevent crash during token refresh
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if user turned notifications OFF in Preferences / SharedPreferences
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                FirebaseServiceEntryPoint::class.java
            )
            if (!entryPoint.getUserPreferencesRepository().isNotificationsEnabledSync()) {
                entryPoint.getCrashlyticsHelper().log("Notification dropped: user disabled notifications")
                return
            }
        } catch (_: Exception) {
            // If entry point resolution fails, fallback to shared preferences directly
            val sp = applicationContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            if (!sp.getBoolean("notifications_enabled", true)) {
                return
            }
        }

        // Ensure Notification Channels are created on Android 8+
        NotificationChannels.createChannels(applicationContext)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: getString(R.string.app_name)
        val body = notification?.body ?: data["message"] ?: data["body"] ?: ""
        val route = data["route"] ?: data["target_route"] ?: "home"
        val campaign = data["campaign"]
        val itemId = data["item_id"]
        val channelId = data["channel_id"] ?: NotificationChannels.CHANNEL_UPDATES

        showNotification(
            title = title,
            body = body,
            route = route,
            campaign = campaign,
            itemId = itemId,
            channelId = channelId
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        route: String,
        campaign: String?,
        itemId: String?,
        channelId: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TARGET_ROUTE, route)
            campaign?.let { putExtra(EXTRA_CAMPAIGN, it) }
            itemId?.let { putExtra(EXTRA_ITEM_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(Color.parseColor("#D4AF37")) // Gold
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
