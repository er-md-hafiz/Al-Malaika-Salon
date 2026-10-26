package com.almalika.salon.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.almalika.salon.core.analytics.AnalyticsHelper
import com.almalika.salon.core.crashlytics.CrashlyticsHelper
import com.almalika.salon.data.notification.AlMalaikaFirebaseMessagingService
import com.almalika.salon.presentation.main.MainAppShell
import com.almalika.salon.presentation.theme.AlMalaikaSalonTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var crashlyticsHelper: CrashlyticsHelper

    private val targetRouteState = mutableStateOf<String?>(null)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            analyticsHelper.logSelectContent("permission", "notification_granted")
            crashlyticsHelper.setCustomKey("notification_permission", "granted")
        } else {
            analyticsHelper.logSelectContent("permission", "notification_denied")
            crashlyticsHelper.setCustomKey("notification_permission", "denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)
        requestNotificationPermission()

        setContent {
            AlMalaikaSalonTheme {
                MainAppShell(
                    initialRoute = targetRouteState.value,
                    onRouteHandled = { targetRouteState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        val route = intent.getStringExtra(AlMalaikaFirebaseMessagingService.EXTRA_TARGET_ROUTE)
            ?: intent.getStringExtra("route")
            ?: intent.getStringExtra("target_route")

        val campaign = intent.getStringExtra(AlMalaikaFirebaseMessagingService.EXTRA_CAMPAIGN)
            ?: intent.getStringExtra("campaign")

        val itemId = intent.getStringExtra(AlMalaikaFirebaseMessagingService.EXTRA_ITEM_ID)
            ?: intent.getStringExtra("item_id")

        if (!route.isNullOrBlank()) {
            targetRouteState.value = route
            analyticsHelper.logNotificationOpen(
                campaign = campaign,
                route = route,
                notificationId = itemId
            )
            crashlyticsHelper.setCustomKey("launch_source", "push_notification")
            crashlyticsHelper.setCustomKey("notification_route", route)
        } else {
            crashlyticsHelper.setCustomKey("launch_source", "normal_open")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            } else {
                crashlyticsHelper.setCustomKey("notification_permission", "granted")
            }
        }
    }
}

