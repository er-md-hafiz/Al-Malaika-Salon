package com.almalika.salon.data.local.asset

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetConfigReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readAssetJson(fileName: String): String {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            "{}"
        }
    }
}
