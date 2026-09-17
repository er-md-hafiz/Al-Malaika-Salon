package com.almalika.salon.core.action

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import androidx.core.net.toUri

object ActionExecutor {

    fun execute(context: Context, action: AppAction) {
        when (action) {
            is AppAction.Call -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:${action.phoneNumber.trim()}".toUri()
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is AppAction.WhatsApp -> {
                try {
                    val formattedNumber = action.phoneNumber.replace("+", "").replace(" ", "").trim()
                    val uri = "https://api.whatsapp.com/send?phone=$formattedNumber&text=${
                        Uri.encode(action.text)
                    }".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.whatsapp")
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    // Fallback to regular browser link if WhatsApp app is not installed
                    try {
                        val formattedNumber = action.phoneNumber.replace("+", "").replace(" ", "").trim()
                        val fallbackUri =
                            "https://api.whatsapp.com/send?phone=$formattedNumber&text=${
                                Uri.encode(action.text)
                            }".toUri()
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                        context.startActivity(fallbackIntent)
                    } catch (fallbackEx: Exception) {
                        Toast.makeText(context, "WhatsApp unavailable: ${fallbackEx.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is AppAction.OpenMap -> {
                try {
                    val uri = if (action.lat != null && action.lng != null && action.lat != 0.0 && action.lng != 0.0) {
                        "geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Salon)".toUri()
                    } else if (action.queryOrUrl.startsWith("http")) {
                        action.queryOrUrl.toUri()
                    } else {
                        "geo:0,0?q=${Uri.encode(action.queryOrUrl)}".toUri()
                    }
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Maps unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is AppAction.ShareApp -> {
                try {
                    val sendIntent: Intent = Intent().apply {
                        this.action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "${action.message}\n${action.url}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Al-Malaika Salon")
                    context.startActivity(shareIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot share: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is AppAction.RateApp -> {
                if (context is Activity) {
                    val manager = ReviewManagerFactory.create(context)
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            manager.launchReviewFlow(context, reviewInfo)
                        } else {
                            // Fallback to direct Play Store intent
                            openPlayStore(context)
                        }
                    }
                } else {
                    openPlayStore(context)
                }
            }

            is AppAction.OpenUrl -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, action.url.toUri())
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                // Screen navigation events are handled by the Compose NavController directly
            }
        }
    }

    private fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri())
            context.startActivity(intent)
        }
    }
}
