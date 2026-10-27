package com.almalika.salon.data.local.asset

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AssetConfigReader {
    private val context: Context?

    @Inject
    constructor(@ApplicationContext context: Context) {
        this.context = context
    }

    constructor() {
        this.context = null
    }

    open fun readAssetJson(fileName: String): String {
        return try {
            context?.assets?.open(fileName)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: "{}"
        } catch (e: Exception) {
            "{}"
        }
    }
}
