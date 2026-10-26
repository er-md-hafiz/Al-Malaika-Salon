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
                    val trimmed = action.phoneNumber.trim()
                    if (trimmed.isBlank()) {
                        Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:$trimmed".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is AppAction.WhatsApp -> {
                val input = action.phoneNumber.trim()
                if (input.isBlank()) {
                    Toast.makeText(context, "WhatsApp contact is not available", Toast.LENGTH_SHORT).show()
                    return
                }
                val rawNumber = if (input.startsWith("http", ignoreCase = true)) {
                    input.substringAfterLast("/").substringAfter("phone=").substringBefore("&")
                } else {
                    input
                }
                val formattedNumber = rawNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
                if (formattedNumber.isBlank()) {
                    Toast.makeText(context, "WhatsApp contact is not available", Toast.LENGTH_SHORT).show()
                    return
                }
                try {
                    val uri = "https://api.whatsapp.com/send?phone=$formattedNumber&text=${
                        Uri.encode(action.text)
                    }".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.whatsapp")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    // Fallback to regular browser link if WhatsApp app is not installed
                    try {
                        val fallbackUri =
                            "https://api.whatsapp.com/send?phone=$formattedNumber&text=${
                                Uri.encode(action.text)
                            }".toUri()
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
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
                    } else if (action.queryOrUrl.isNotBlank()) {
                        "geo:0,0?q=${Uri.encode(action.queryOrUrl)}".toUri()
                    } else {
                        Toast.makeText(context, "Location coordinates not available", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Maps unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            is AppAction.ShareApp -> {
                try {
                    val textToShare = buildString {
                        if (action.message.isNotBlank()) append(action.message)
                        if (action.url.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append(action.url)
                        }
                    }
                    val sendIntent: Intent = Intent().apply {
                        this.action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Al-Malaika Salon").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
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
                    val trimmed = action.url.trim()
                    if (trimmed.isBlank()) {
                        Toast.makeText(context, "Link is not available", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val formattedUri = if (trimmed.startsWith("mailto:")) {
                        trimmed.toUri()
                    } else if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                        "https://$trimmed".toUri()
                    } else {
                        trimmed.toUri()
                    }
                    val intent = Intent(Intent.ACTION_VIEW, formattedUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
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
            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
