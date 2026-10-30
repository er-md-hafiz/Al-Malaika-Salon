package com.almalika.salon.core.config

import com.almalika.salon.data.remote.config.AppConfig
import com.almalika.salon.data.remote.config.Metadata
import com.almalika.salon.data.remote.config.RemoteConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigValidatorTest {

    @Test
    fun testValidConfigPasses() {
        val config = RemoteConfig(
            metadata = Metadata(schemaVersion = "1.2.0"),
            appConfig = AppConfig(minimumSupportedConfigVersion = 1)
        )
        val result = ConfigValidator.validate(config)
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun testMajorSchemaMismatchRejected() {
        val config = RemoteConfig(
            metadata = Metadata(schemaVersion = "2.0.0"),
            appConfig = AppConfig(minimumSupportedConfigVersion = 1)
        )
        val result = ConfigValidator.validate(config)
        assertTrue(result is ValidationResult.Error)
        assertEquals("SCHEMA_MISMATCH: Unsupported schema major version 2. Expected 1", (result as ValidationResult.Error).reason)
    }

    @Test
    fun testUnsupportedClientVersionRejected() {
        val config = RemoteConfig(
            metadata = Metadata(schemaVersion = "1.0.0"),
            appConfig = AppConfig(minimumSupportedConfigVersion = 999)
        )
        val result = ConfigValidator.validate(config)
        assertTrue(result is ValidationResult.Error)
    }
}

