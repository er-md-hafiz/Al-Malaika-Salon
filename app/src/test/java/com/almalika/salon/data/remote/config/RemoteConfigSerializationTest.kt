package com.almalika.salon.data.remote.config

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RemoteConfigSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun testParseFullMenuJsonRoundtrip() {
        val jsonString = """
        {
          "metadata": {
            "schemaVersion": "1.2.0",
            "uiConfigVersion": 3,
            "lastUpdated": "2026-10-28",
            "locale": "en-IN",
            "currencyCode": "INR",
            "currencySymbol": "₹"
          },
          "appConfig": {
            "appName": "Al-Malaika Salon",
            "tagline": "Royal Grooming. Timeless Style.",
            "defaultLanguage": "en",
            "maintenanceMode": false
          },
          "theme": {
            "themeMode": "dark",
            "colors": {
              "primary": "#D4AF37",
              "background": "#101010",
              "onPrimary": "#101010"
            }
          },
          "startup": {
            "splashScreenId": "splash",
            "defaultDestination": "home"
          },
          "pages": {
            "aboutUs": "https://almalaika.example/about",
            "privacyPolicyUrl": "https://almalaika.example/privacy"
          },
          "banners": [
            {
              "id": 1,
              "title": "Welcome",
              "action": {
                "type": "navigate",
                "destination": "services"
              }
            }
          ],
          "galleryItems": [
            {
              "id": 101,
              "title": "Haircut 1",
              "imageUrl": "https://images.example/1.jpg"
            }
          ],
          "reviews": [
            {
              "id": 1,
              "customerName": "Demo User",
              "rating": 5,
              "source": "demo",
              "isVerified": false
            }
          ],
          "franchise": {
            "ctaUrl": "https://example.com/franchise",
            "formUrl": "https://example.com/form"
          }
        }
        """.trimIndent()

        val config = json.decodeFromString<RemoteConfig>(jsonString)
        assertEquals("1.2.0", config.metadata.schemaVersion)
        assertEquals("₹", config.metadata.currencySymbol)
        assertEquals("#D4AF37", config.theme.colors.primary)
        assertEquals("#101010", config.theme.colors.onPrimary)

        // Banner id serializer handles Int id cleanly
        assertEquals("1", config.banners.first().id)
        assertEquals("navigate", config.banners.first().action?.type)

        // Gallery item category regression (R2)
        assertNull(config.galleryItems.first().category)
        assertTrue(config.galleryItems.first().isAvailable)

        // Review sample data flag
        assertTrue(config.reviews.first().isSample)

        // Pages url resolution
        assertEquals("https://almalaika.example/about", config.pages.resolve("aboutUs"))
    }

    @Test
    fun testParseFallbackAssetJson() {
        val assetFile = File("src/main/assets/fallback_config.json")
        if (assetFile.exists()) {
            val fileContent = assetFile.readText()
            val config = json.decodeFromString<RemoteConfig>(fileContent)
            assertNotNull(config.metadata)
            assertNotNull(config.appConfig)
        }
    }
}
