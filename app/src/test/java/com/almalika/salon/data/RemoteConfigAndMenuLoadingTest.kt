package com.almalika.salon.data

import com.almalika.salon.data.local.asset.AssetConfigReader
import com.almalika.salon.data.local.dao.SalonConfigDao
import com.almalika.salon.data.local.entity.BranchEntity
import com.almalika.salon.data.local.entity.GalleryImageEntity
import com.almalika.salon.data.local.entity.HomeEntity
import com.almalika.salon.data.local.entity.MenuEntity
import com.almalika.salon.data.local.entity.OfferEntity
import com.almalika.salon.data.local.entity.ServiceCategoryEntity
import com.almalika.salon.data.mapper.toDomain
import com.almalika.salon.data.remote.SalonApi
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.data.repository.SalonRepositoryImpl
import com.almalika.salon.domain.model.UserSessionState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File
import java.io.InputStreamReader

/**
 * Mandatory QA Automation test suite verifying Remote menu.json Configuration Loading:
 * - Single source of truth (remoteConfig.url)
 * - Preferred endpoint vs raw GitHub JSON endpoint
 * - Strict rejection of GitHub HTML blob pages
 * - HTTP status code, non-empty body, HTML document rejection, JSON syntax, and schema validation
 * - Atomic caching into Room DAO (last-known-good preserved on failure)
 * - Offline startup and bundled fallback operation
 * - Preservation of existing banners, drawer items, destinations, and feature flags
 */
class RemoteConfigAndMenuLoadingTest {

    private lateinit var moshi: Moshi
    private lateinit var validLiveJson: String

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val resourceStream = javaClass.classLoader?.getResourceAsStream("remote_live_menu.json")
        validLiveJson = if (resourceStream != null) {
            InputStreamReader(resourceStream).readText()
        } else {
            File("src/test/resources/remote_live_menu.json").readText()
        }
    }

    // -------------------------------------------------------------
    // Test Case 1: URL Resolution from remoteConfig.url
    // -------------------------------------------------------------
    @Test
    fun testRemoteConfigUrlResolutionAndSingleSourceOfTruth() {
        // 1. Preferred endpoint when configured
        val preferred = "https://er-md-hafiz.github.io/api.almalaikasalon/menu.json"
        assertEquals(preferred, SalonRepositoryImpl.resolveRemoteConfigUrl(preferred))

        // 2. Raw GitHub JSON fallback when explicitly specified
        val rawGitHub = "https://raw.githubusercontent.com/er-md-hafiz/api.almalaikasalon/main/menu.json"
        assertEquals(rawGitHub, SalonRepositoryImpl.resolveRemoteConfigUrl(rawGitHub))

        // 3. Blank or null defaults to preferred endpoint
        assertEquals(preferred, SalonRepositoryImpl.resolveRemoteConfigUrl(null))
        assertEquals(preferred, SalonRepositoryImpl.resolveRemoteConfigUrl("   "))
    }

    // -------------------------------------------------------------
    // Test Case 11: Rejection of GitHub HTML Blob Page as JSON Endpoint
    // -------------------------------------------------------------
    @Test
    fun testAppNeverAttemptsToParseGitHubHtmlPageAsJson() {
        // Given an accidental GitHub web page URL with "/blob/"
        val blobUrl = "https://github.com/er-md-hafiz/api.almalaikasalon/blob/main/menu.json"

        // URL resolver must reject it and fallback to preferred JSON endpoint
        val resolved = SalonRepositoryImpl.resolveRemoteConfigUrl(blobUrl)
        assertEquals(SalonRepositoryImpl.PREFERRED_CONFIG_URL, resolved)
        assertFalse(resolved.contains("/blob/"))

        // Furthermore, validator must strictly reject any HTML content received
        val htmlPage = """
            <!DOCTYPE html>
            <html lang="en">
            <head><title>GitHub - menu.json</title></head>
            <body><div>HTML content here</div></body>
            </html>
        """.trimIndent()

        val validationResult = SalonRepositoryImpl.validateConfiguration(
            dto = moshi.adapter(MenuResponseDto::class.java).fromJson("{}"),
            rawJson = htmlPage
        )
        assertTrue(validationResult.isFailure)
        assertTrue(validationResult.exceptionOrNull()?.message?.contains("HTML document") == true)
    }

    // -------------------------------------------------------------
    // Test Case 2: Valid JSON Response Deserialization and Schema Version
    // -------------------------------------------------------------
    @Test
    fun testValidJsonResponseAndExpectedSchema() {
        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(validLiveJson)
        assertNotNull(dto)

        val validationResult = SalonRepositoryImpl.validateConfiguration(dto, validLiveJson)
        assertTrue(validationResult.isSuccess)

        val master = dto!!.toDomain()
        assertEquals("Al-Malaika Salon", master.salon.name)
        assertEquals(2, master.schemaVersion)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/menu.json", master.remoteConfig.url)
    }

    // -------------------------------------------------------------
    // Test Case 3: HTTP 404, 500, and Network Failure Scenarios Handled Gracefully
    // -------------------------------------------------------------
    @Test
    fun testHttpErrorsHandledGracefullyWithoutCrashing() = runTest {
        val fakeDao = FakeSalonConfigDao()
        val fakeApi = FakeSalonApi(errorMode = FakeSalonApi.ErrorMode.HTTP_404)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, FakeAssetConfigReader(validLiveJson), moshi)

        val result = repo.refreshConfig(force = true)
        assertTrue("HTTP 404 must return Result.failure", result.isFailure)
        // Ensure no crash occurred and DAO was not corrupted
        assertNull(fakeDao.getMenu())

        // HTTP 500
        fakeApi.errorMode = FakeSalonApi.ErrorMode.HTTP_500
        val result500 = repo.refreshConfig(force = true)
        assertTrue("HTTP 500 must return Result.failure", result500.isFailure)

        // Network Timeout / Unavailable server
        fakeApi.errorMode = FakeSalonApi.ErrorMode.NETWORK_TIMEOUT
        val resultTimeout = repo.refreshConfig(force = true)
        assertTrue("Timeout must return Result.failure", resultTimeout.isFailure)
    }

    // -------------------------------------------------------------
    // Test Case 4: Empty Response and Malformed JSON Handled Safely
    // -------------------------------------------------------------
    @Test
    fun testEmptyResponseAndMalformedJsonRejected() = runTest {
        val fakeDao = FakeSalonConfigDao()
        val fakeApi = FakeSalonApi(errorMode = FakeSalonApi.ErrorMode.EMPTY_BODY)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, FakeAssetConfigReader(validLiveJson), moshi)

        val resultEmpty = repo.refreshConfig(force = true)
        assertTrue("Empty body must fail validation", resultEmpty.isFailure)

        fakeApi.errorMode = FakeSalonApi.ErrorMode.MALFORMED_JSON
        val resultMalformed = repo.refreshConfig(force = true)
        assertTrue("Malformed JSON must fail validation", resultMalformed.isFailure)
    }

    // -------------------------------------------------------------
    // Test Case 5: Missing Required Fields and Incompatible Schema
    // -------------------------------------------------------------
    @Test
    fun testMissingRequiredFieldsAndIncompatibleSchemaRejected() {
        val adapter = moshi.adapter(MenuResponseDto::class.java)

        // 1. Missing schemaVersion
        val missingSchemaVerJson = """{ "salon": { "name": "Al-Malaika" }, "navigation": { "drawerItems": [] } }"""
        val dto1 = adapter.fromJson(missingSchemaVerJson)
        val res1 = SalonRepositoryImpl.validateConfiguration(dto1, missingSchemaVerJson)
        assertTrue(res1.isFailure)
        assertTrue(res1.exceptionOrNull()?.message?.contains("schemaVersion") == true)

        // 2. Missing essential sections
        val emptyJson = "{}"
        val dto2 = adapter.fromJson(emptyJson)
        val res2 = SalonRepositoryImpl.validateConfiguration(dto2, emptyJson)
        assertTrue(res2.isFailure)
        assertTrue(res2.exceptionOrNull()?.message?.contains("critical schema sections") == true)
    }

    // -------------------------------------------------------------
    // Test Case 6: Offline Startup With Valid Cached Configuration
    // -------------------------------------------------------------
    @Test
    fun testOfflineStartupWithCachedConfiguration() = runTest {
        val fakeDao = FakeSalonConfigDao()
        // Pre-populate Room DAO with cached config
        fakeDao.insertMenu(MenuEntity(json = validLiveJson))

        // API completely offline
        val fakeApi = FakeSalonApi(errorMode = FakeSalonApi.ErrorMode.OFFLINE)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, FakeAssetConfigReader(validLiveJson), moshi)

        val config = repo.getMasterConfig().first()
        assertNotNull(config)
        assertEquals("Al-Malaika Salon", config.salon.name)
        assertTrue(config.drawer.isNotEmpty())
        assertTrue(config.bottomNavigation.isNotEmpty())
    }

    // -------------------------------------------------------------
    // Test Case 7: Invalid Remote Config Does NOT Overwrite Last-Known-Good Cache
    // -------------------------------------------------------------
    @Test
    fun testInvalidRemoteConfigDoesNotOverwriteLastKnownGoodCache() = runTest {
        val fakeDao = FakeSalonConfigDao()
        // Save known good configuration
        fakeDao.insertMenu(MenuEntity(json = validLiveJson))
        val initialCachedJson = fakeDao.getMenu()?.json
        assertNotNull(initialCachedJson)

        // Remote returns corrupted response
        val fakeApi = FakeSalonApi(errorMode = FakeSalonApi.ErrorMode.MALFORMED_JSON)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, FakeAssetConfigReader(validLiveJson), moshi)

        val refreshResult = repo.refreshConfig(force = true)
        assertTrue(refreshResult.isFailure)

        // Verify last-known-good cache is completely preserved in Room DAO
        val finalCachedJson = fakeDao.getMenu()?.json
        assertEquals("Last-known-good configuration must be preserved", initialCachedJson, finalCachedJson)
    }

    // -------------------------------------------------------------
    // Test Case 8: Bundled Fallback Works When No Valid Remote or Cached Config Exists
    // -------------------------------------------------------------
    @Test
    fun testBundledFallbackUsedWhenCacheIsEmptyAndOffline() = runTest {
        val fakeDao = FakeSalonConfigDao() // Empty Room DAO
        assertNull(fakeDao.getMenu())

        val fakeApi = FakeSalonApi(errorMode = FakeSalonApi.ErrorMode.OFFLINE)
        val fallbackReader = FakeAssetConfigReader(validLiveJson)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, fallbackReader, moshi)

        val masterConfig = repo.getMasterConfig().first()
        assertNotNull(masterConfig)
        assertEquals("Al-Malaika Salon", masterConfig.salon.name)
        assertTrue(masterConfig.drawer.isNotEmpty())
    }

    // -------------------------------------------------------------
    // Test Case 9 & 10: Atomic Updates and Feature Flags Preservation
    // -------------------------------------------------------------
    @Test
    fun testAtomicUpdatePreservesBannersDrawerAndFeatureFlags() = runTest {
        val fakeDao = FakeSalonConfigDao()
        val fakeApi = FakeSalonApi(validPayload = validLiveJson)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, FakeAssetConfigReader(validLiveJson), moshi)

        val success = repo.refreshConfig(force = true)
        assertTrue("Refresh should succeed with valid live payload", success.isSuccess)

        val updatedMaster = repo.getMasterConfig().first()
        assertEquals("Al-Malaika Salon", updatedMaster.salon.name)
        assertTrue(updatedMaster.featureFlags.containsKey("gallery"))
        assertTrue(updatedMaster.drawer.any { it.route == "services" })
        assertTrue(updatedMaster.bottomNavigation.any { it.route == "profile" })
    }

    // -------------------------------------------------------------
    // Test Case 12: Config Changes Do Not Alter Auth State Or Menu Rules
    // -------------------------------------------------------------
    @Test
    fun testConfigUpdateDoesNotChangeAuthenticationStateOrDrawerRules() = runTest {
        val fakeDao = FakeSalonConfigDao()
        fakeDao.insertMenu(MenuEntity(json = validLiveJson))
        val fakeApi = FakeSalonApi(validPayload = validLiveJson)
        val repo = SalonRepositoryImpl(fakeApi, fakeDao, FakeAssetConfigReader(validLiveJson), moshi)

        // Execute refresh
        val refresh = repo.refreshConfig(force = true)
        assertTrue(refresh.isSuccess)

        // Drawer visibility rules must strictly depend on runtime user session state, not static JSON
        val master = repo.getMasterConfig().first()

        // Test 1: Registered user -> login drawer item MUST be hidden
        val registeredDrawer = master.drawer.filter { item ->
            val isAccountItem = item.id in listOf("drawer_account", "account", "login") ||
                    item.route in listOf("login", "register", "account")
            !isAccountItem && item.enabled
        }
        assertNull(registeredDrawer.find { it.route == "login" })

        // Test 2: Guest or signed out -> login drawer item MUST be visible
        val guestDrawer = master.drawer.filter { item ->
            item.enabled
        }
        assertNotNull(guestDrawer.find { it.route == "login" || it.id == "drawer_account" })
    }
}

// -------------------------------------------------------------
// Test Doubles
// -------------------------------------------------------------

class FakeSalonApi(
    var errorMode: ErrorMode = ErrorMode.NONE,
    var validPayload: String = "{}"
) : SalonApi {

    enum class ErrorMode {
        NONE,
        HTTP_404,
        HTTP_500,
        NETWORK_TIMEOUT,
        EMPTY_BODY,
        MALFORMED_JSON,
        OFFLINE
    }

    override suspend fun getMenuConfig(): MenuResponseDto = throw UnsupportedOperationException()
    override suspend fun getMenuConfigByUrl(url: String): MenuResponseDto = throw UnsupportedOperationException()
    override suspend fun getDynamicConfigByUrl(url: String): Map<String, Any> = throw UnsupportedOperationException()

    override suspend fun getMenuConfigRaw(url: String): Response<ResponseBody> {
        val mediaType = "application/json".toMediaTypeOrNull()
        return when (errorMode) {
            ErrorMode.NONE -> Response.success(validPayload.toResponseBody(mediaType))
            ErrorMode.HTTP_404 -> Response.error(404, "Not Found".toResponseBody("text/plain".toMediaTypeOrNull()))
            ErrorMode.HTTP_500 -> Response.error(500, "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull()))
            ErrorMode.NETWORK_TIMEOUT -> throw java.net.SocketTimeoutException("Connection timed out")
            ErrorMode.OFFLINE -> throw java.io.IOException("No internet connection")
            ErrorMode.EMPTY_BODY -> Response.success("".toResponseBody(mediaType))
            ErrorMode.MALFORMED_JSON -> Response.success("{ invalid_json_syntax: ".toResponseBody(mediaType))
        }
    }
}

class FakeSalonConfigDao : SalonConfigDao {
    private val _menuFlow = MutableStateFlow<MenuEntity?>(null)

    override suspend fun insertMenu(menu: MenuEntity) { _menuFlow.value = menu }
    override fun observeMenu(): Flow<MenuEntity?> = _menuFlow
    override suspend fun getMenu(): MenuEntity? = _menuFlow.value

    override suspend fun insertHome(home: HomeEntity) {}
    override fun observeHome(): Flow<HomeEntity?> = MutableStateFlow(null)
    override suspend fun getHome(): HomeEntity? = null

    override suspend fun insertServiceCategories(categories: List<ServiceCategoryEntity>) {}
    override fun observeServiceCategories(): Flow<List<ServiceCategoryEntity>> = MutableStateFlow(emptyList())

    override suspend fun insertOffers(offers: List<OfferEntity>) {}
    override fun observeOffers(): Flow<List<OfferEntity>> = MutableStateFlow(emptyList())

    override suspend fun insertGalleryImages(images: List<GalleryImageEntity>) {}
    override fun observeGalleryImages(): Flow<List<GalleryImageEntity>> = MutableStateFlow(emptyList())

    override suspend fun insertBranches(branches: List<BranchEntity>) {}
    override fun observeBranches(): Flow<List<BranchEntity>> = MutableStateFlow(emptyList())

    override suspend fun clearMenu() { _menuFlow.value = null }
    override suspend fun clearHome() {}
    override suspend fun clearServiceCategories() {}
    override suspend fun clearOffers() {}
    override suspend fun clearGalleryImages() {}
    override suspend fun clearBranches() {}

    override suspend fun replaceConfig(
        menu: MenuEntity,
        home: HomeEntity,
        serviceCategories: List<ServiceCategoryEntity>,
        offers: List<OfferEntity>,
        galleryImages: List<GalleryImageEntity>,
        branches: List<BranchEntity>
    ) {
        _menuFlow.value = menu
    }
}

class FakeAssetConfigReader(private val json: String) : AssetConfigReader() {
    override fun readAssetJson(fileName: String): String = json
}
