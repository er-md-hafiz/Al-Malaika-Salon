package com.almalika.salon.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import com.almalika.salon.data.mapper.toDomain
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.domain.model.SalonMasterConfig
import com.almalika.salon.presentation.main.getScreenTitle
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader

class NavigationAndLayoutTest {

    private lateinit var moshi: Moshi
    private var liveMaster: SalonMasterConfig? = null

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val stream = javaClass.classLoader?.getResourceAsStream("remote_live_menu.json")
        if (stream != null) {
            val json = InputStreamReader(stream).readText()
            val dto = moshi.adapter(MenuResponseDto::class.java).fromJson(json)
            liveMaster = dto?.toDomain()
        }
    }

    @Test
    fun `test all 14 drawer items exist and remain accessible for scrolling`() {
        val master = liveMaster ?: return
        val drawerItems = master.drawer.filter { it.enabled }.sortedBy { it.order }

        assertTrue("Drawer must contain all menu items", drawerItems.size >= 14)
        val routes = drawerItems.map { it.route }.toSet()

        assertTrue(routes.contains("home"))
        assertTrue(routes.contains("services"))
        assertTrue(routes.contains("gallery"))
        assertTrue(routes.contains("reviews"))
        assertTrue(routes.contains("locations"))
        assertTrue(routes.contains("about"))
        assertTrue(routes.contains("contact"))
        assertTrue(routes.contains("settings"))
        assertTrue(routes.contains("offers"))
        assertTrue(routes.contains("login"))
        assertTrue(routes.contains("terms"))
        assertTrue(routes.contains("privacy"))
    }

    @Test
    fun `test contact is removed from bottom navigation and replaced with profile`() {
        val master = liveMaster ?: return
        val bottomItems = master.bottomNavigation.filter { it.enabled }.sortedBy { it.order }

        // Contact must not be in bottom navigation
        val routes = bottomItems.map { it.route }
        assertFalse("Contact must NOT be in bottom navigation", routes.contains("contact"))
        assertTrue("Profile MUST be present in bottom navigation", routes.contains("profile"))

        val profileItem = bottomItems.find { it.route == "profile" }
        assertNotNull(profileItem)
        assertEquals("Profile", profileItem?.title)
        assertTrue(profileItem?.icon in listOf("account_circle", "person"))

        // Must maintain clean 5 destinations
        assertEquals(5, bottomItems.size)
    }

    @Test
    fun `test screen title mapping for profile and settings`() {
        val master = liveMaster!!
        assertEquals("Profile & Settings", getScreenTitle("profile", master))
        assertEquals("Settings", getScreenTitle("settings", master))
        assertEquals("My Account", getScreenTitle("account", master))
        assertEquals("Contact Us", getScreenTitle("contact", master))
    }

    @Test
    fun `test icon resolver maps profile person account to AccountCircle`() {
        val personIcon = IconResolver.resolve("person")
        val profileIcon = IconResolver.resolve("profile")
        val accountIcon = IconResolver.resolve("account")

        assertEquals(Icons.Default.AccountCircle, personIcon)
        assertEquals(Icons.Default.AccountCircle, profileIcon)
        assertEquals(Icons.Default.AccountCircle, accountIcon)
    }
}
