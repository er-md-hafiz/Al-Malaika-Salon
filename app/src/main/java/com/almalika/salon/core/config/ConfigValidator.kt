package com.almalika.salon.core.config

import com.almalika.salon.data.remote.config.RemoteConfig

sealed class ValidationResult {
    data class Success(val warnings: List<String> = emptyList()) : ValidationResult()
    data class Error(val reason: String, val warnings: List<String> = emptyList()) : ValidationResult()
}

object ConfigValidator {

    private const val SUPPORTED_MAJOR_VERSION = "1"
    private const val CLIENT_CONFIG_VERSION = 1

    fun validate(config: RemoteConfig): ValidationResult {
        val warnings = mutableListOf<String>()

        // 1. Schema version check
        val schemaVersion = config.metadata.schemaVersion
        val majorVersion = schemaVersion.split(".").firstOrNull() ?: ""
        if (majorVersion != SUPPORTED_MAJOR_VERSION) {
            return ValidationResult.Error(
                reason = "SCHEMA_MISMATCH: Unsupported schema major version $majorVersion. Expected $SUPPORTED_MAJOR_VERSION",
                warnings = warnings
            )
        }

        // 2. Minimum supported config version
        if (config.appConfig.minimumSupportedConfigVersion > CLIENT_CONFIG_VERSION) {
            return ValidationResult.Error(
                reason = "CONFIG_VERSION_TOO_NEW: Config version ${config.appConfig.minimumSupportedConfigVersion} > client $CLIENT_CONFIG_VERSION",
                warnings = warnings
            )
        }

        // 3. Data source existence checks (log warnings, don't reject)
        config.home.sections.forEach { section ->
            val ds = section.dataSource
            if (ds != null) {
                val exists = when (ds) {
                    "banners" -> config.banners.isNotEmpty()
                    "quickServices" -> config.quickServices.isNotEmpty()
                    "popularServices" -> config.popularServices.isNotEmpty()
                    "homeServices" -> config.homeServices.isNotEmpty()
                    "whyChooseUs" -> config.whyChooseUs.isNotEmpty()
                    "gallery", "galleryItems" -> config.galleryItems.isNotEmpty()
                    "reviews" -> config.reviews.isNotEmpty()
                    "barbers" -> config.barbers.isNotEmpty()
                    "offers" -> config.offers.isNotEmpty()
                    "priceList" -> config.priceList.isNotEmpty()
                    "franchise" -> config.franchise != null
                    "salon" -> config.salon != null
                    else -> false
                }
                if (!exists) {
                    warnings.add("WARN_MISSING_DATASOURCE: dataSource '$ds' in section '${section.id}' is empty or missing")
                }
            }
        }

        // 4. Franchise CTA validation check
        config.franchise?.let { franchise ->
            if (franchise.ctaUrl?.contains("example.com") == true) {
                warnings.add("WARN_INVALID_URL: franchise.ctaUrl is a placeholder: ${franchise.ctaUrl}")
            }
            if (franchise.formUrl?.contains("example.com") == true) {
                warnings.add("WARN_INVALID_URL: franchise.formUrl is a placeholder: ${franchise.formUrl}")
            }
        }

        // 5. Rating drift check (R7)
        config.salon?.let { salon ->
            if (config.reviews.isNotEmpty()) {
                val computedAvg = config.reviews.map { it.rating }.average().toFloat()
                if (kotlin.math.abs(salon.rating - computedAvg) > 0.1f) {
                    warnings.add("WARN_RATING_DRIFT: Declared rating ${salon.rating} drifts from computed $computedAvg")
                }
            }
        }

        return ValidationResult.Success(warnings)
    }
}

