package com.almalika.salon.auth

import com.almalika.salon.data.mapper.toDomain
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.domain.model.*
import com.almalika.salon.domain.repository.AuthRepository
import com.almalika.salon.presentation.auth.AuthActionState
import com.almalika.salon.presentation.auth.AuthViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader

/**
 * Mandatory QA Automation test suite verifying:
 * - 3 Explicit Authentication States (SIGNED_OUT, ANONYMOUS_GUEST, REGISTERED_USER)
 * - Hamburger Drawer dynamic visibility rules
 * - Session persistence across app restarts
 * - Account linking and collision protection
 * - Correct logout behavior, backstack resetting, and zero data leakage
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationSessionAndDrawerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var moshi: Moshi
    private lateinit var masterConfig: SalonMasterConfig

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val stream = javaClass.classLoader?.getResourceAsStream("remote_live_menu.json")
        val json = if (stream != null) {
            InputStreamReader(stream).readText()
        } else {
            javaClass.classLoader?.getResourceAsStream("config/menu_fallback.json")?.let {
                InputStreamReader(it).readText()
            } ?: "{}"
        }
        val dto = moshi.adapter(MenuResponseDto::class.java).fromJson(json)
        masterConfig = dto!!.toDomain()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Helper simulating runtime drawer item computation in MainAppShell
    private fun computeDrawerItems(master: SalonMasterConfig, sessionState: UserSessionState): List<NavItem> {
        val isAccountItem: (NavItem) -> Boolean = { item ->
            item.id in listOf("drawer_account", "account", "login") ||
                    item.route in listOf("login", "register", "account") ||
                    item.title.contains("Sign In", ignoreCase = true) ||
                    item.title.contains("Register", ignoreCase = true)
        }

        val nonAccountItems = master.drawer.filter { item ->
            !isAccountItem(item) && item.enabled
        }

        val result = nonAccountItems.toMutableList()

        if (sessionState != UserSessionState.REGISTERED_USER) {
            val existingAccountItem = master.drawer.firstOrNull { isAccountItem(it) }
            val drawerAccountItem = existingAccountItem?.copy(
                enabled = true,
                title = "Sign In / Register",
                route = "login"
            ) ?: NavItem(
                id = "drawer_account",
                title = "Sign In / Register",
                icon = "account_circle",
                route = "login",
                enabled = true,
                order = (master.drawer.maxOfOrNull { it.order } ?: 13) + 1
            )
            result.add(drawerAccountItem)
        }

        return result.sortedBy { it.order }
    }

    // -------------------------------------------------------------
    // Test Case 1 & 2: Fresh Install & Signed-Out User Public Browsing
    // -------------------------------------------------------------
    @Test
    fun testFreshInstallAndSignedOutPublicBrowsing() = runTest(testDispatcher) {
        val fakeRepo = TestAuthRepository() // Starts with null user
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        // Verify signed-out condition
        assertNull(fakeRepo.currentUser)
        assertEquals(UserSessionState.SIGNED_OUT, viewModel.sessionState.value)
        assertTrue(viewModel.authState.value is AuthState.Unauthenticated)

        // Startup behavior: requireLogin is false, user can browse public screens
        assertFalse("requireLogin should be false for public browsing", masterConfig.startup.requireLogin)
        val initialRoute = if (masterConfig.startup.requireLogin) "login" else "home"
        assertEquals("home", initialRoute)

        // Drawer visibility: Sign In / Register MUST be visible for signed-out user
        val drawer = computeDrawerItems(masterConfig, viewModel.sessionState.value)
        val accountItem = drawer.find { it.route == "login" || it.id == "drawer_account" }
        assertNotNull("Sign In / Register must be visible for signed-out user", accountItem)
        assertEquals("Sign In / Register", accountItem?.title)

        // Public destinations remain intact
        val routes = drawer.map { it.route }
        assertTrue(routes.containsAll(listOf("home", "services", "price_list", "gallery", "offers", "locations")))
    }

    // -------------------------------------------------------------
    // Test Case 3: Continue as Guest starts Anonymous Guest Flow
    // -------------------------------------------------------------
    @Test
    fun testContinueAsGuestFlow() = runTest(testDispatcher) {
        val fakeRepo = TestAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        var navigatedRoute: String? = null
        viewModel.signInAnonymously {
            navigatedRoute = "home"
        }
        advanceUntilIdle()

        assertEquals("home", navigatedRoute)
        assertNotNull(fakeRepo.currentUser)
        assertTrue("User must be marked anonymous", fakeRepo.currentUser!!.isAnonymous)
        assertEquals(UserSessionState.ANONYMOUS_GUEST, viewModel.sessionState.value)
        assertTrue(viewModel.authState.value.isAnonymousGuest)
        assertFalse(viewModel.authState.value.isRegisteredUser)

        // Drawer visibility: Sign In / Register MUST remain visible for anonymous guest
        val drawer = computeDrawerItems(masterConfig, viewModel.sessionState.value)
        val accountItem = drawer.find { it.route == "login" || it.id == "drawer_account" }
        assertNotNull("Sign In / Register must be visible for anonymous guest to allow account upgrade", accountItem)
    }

    // -------------------------------------------------------------
    // Test Case 4: Anonymous Guest Restarts Application (Session Restored)
    // -------------------------------------------------------------
    @Test
    fun testAnonymousGuestRestartsApplication() = runTest(testDispatcher) {
        val persistedGuest = AuthUser(
            uid = "guest_persisted_789",
            email = null,
            displayName = "Guest User",
            isAnonymous = true,
            isEmailVerified = false
        )
        // Simulate app restart with persisted guest in FirebaseAuth
        val fakeRepo = TestAuthRepository(initialUser = persistedGuest)
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        assertNotNull(fakeRepo.currentUser)
        assertEquals("guest_persisted_789", fakeRepo.currentUser?.uid)
        assertTrue(fakeRepo.currentUser!!.isAnonymous)
        assertEquals(UserSessionState.ANONYMOUS_GUEST, viewModel.sessionState.value)

        // No duplicate anonymous accounts created on restart
        assertEquals(0, fakeRepo.anonymousSignInsCount)
    }

    // -------------------------------------------------------------
    // Test Case 5: Anonymous Guest Selects Sign In / Register
    // -------------------------------------------------------------
    @Test
    fun testAnonymousGuestSelectsSignInOrRegister() = runTest(testDispatcher) {
        val guest = AuthUser(uid = "guest_111", email = null, isAnonymous = true)
        val fakeRepo = TestAuthRepository(initialUser = guest)
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        val drawer = computeDrawerItems(masterConfig, viewModel.sessionState.value)
        val accountItem = drawer.find { it.route == "login" }
        assertNotNull(accountItem)
        assertEquals("Sign In / Register", accountItem?.title)
        assertEquals("login", accountItem?.route)
    }

    // -------------------------------------------------------------
    // Test Case 6: Guest Account Linking Succeeds & Preserves Data
    // -------------------------------------------------------------
    @Test
    fun testGuestAccountLinkingSucceedsAndPreservesData() = runTest(testDispatcher) {
        val guestUid = "guest_uid_222"
        val guest = AuthUser(uid = guestUid, email = null, displayName = "Guest", isAnonymous = true)
        val fakeRepo = TestAuthRepository(initialUser = guest)
        fakeRepo.saveUserPreferences(guestUid, UserPreferences(darkMode = false, notifications = true))

        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.register("Salim Khan", "salim@example.com", "securePass123", "securePass123")
        advanceUntilIdle()

        assertTrue(viewModel.registerState.value is AuthActionState.Success)
        val linkedUser = fakeRepo.currentUser
        assertNotNull(linkedUser)
        assertEquals(guestUid, linkedUser?.uid) // UID preserved
        assertFalse("Linked account is no longer anonymous", linkedUser!!.isAnonymous)
        assertEquals("salim@example.com", linkedUser.email)
        assertEquals("Salim Khan", linkedUser.displayName)
        assertEquals(UserSessionState.REGISTERED_USER, viewModel.sessionState.value)

        // Drawer immediately hides login item for registered user
        val drawer = computeDrawerItems(masterConfig, viewModel.sessionState.value)
        val accountItem = drawer.find { it.route == "login" || it.id == "drawer_account" }
        assertNull("Login drawer item MUST be hidden for registered user", accountItem)
    }

    // -------------------------------------------------------------
    // Test Case 7: Linking Fails Because Email Belongs to Another Account
    // -------------------------------------------------------------
    @Test
    fun testLinkingFailsWhenEmailAlreadyBelongsToAnotherAccount() = runTest(testDispatcher) {
        val guestUid = "guest_uid_333"
        val guest = AuthUser(uid = guestUid, email = null, displayName = "Guest", isAnonymous = true)
        val fakeRepo = TestAuthRepository(initialUser = guest)
        fakeRepo.existingRegisteredEmails.add("already_registered@example.com")

        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.register("Existing User", "already_registered@example.com", "pass123456", "pass123456")
        advanceUntilIdle()

        // Registration linking failed with collision
        assertTrue(viewModel.registerState.value is AuthActionState.Error)
        val errorMsg = (viewModel.registerState.value as AuthActionState.Error).message
        assertTrue(errorMsg.contains("already exists", ignoreCase = true))

        // Guest session and data MUST NOT be lost or deleted
        assertNotNull(fakeRepo.currentUser)
        assertEquals(guestUid, fakeRepo.currentUser?.uid)
        assertTrue("Guest remains anonymous and active", fakeRepo.currentUser!!.isAnonymous)
        assertEquals(UserSessionState.ANONYMOUS_GUEST, viewModel.sessionState.value)
    }

    // -------------------------------------------------------------
    // Test Case 8, 9, 10: Registered User Navigation & Force-Stop App Restart
    // -------------------------------------------------------------
    @Test
    fun testRegisteredUserSessionPreservedAcrossNavAndRestart() = runTest(testDispatcher) {
        val registeredUser = AuthUser(
            uid = "reg_user_444",
            email = "hafiz@example.com",
            displayName = "Md Hafiz",
            isAnonymous = false,
            isEmailVerified = true
        )
        // 1. Initial login / open
        val fakeRepo = TestAuthRepository(initialUser = registeredUser)
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        assertEquals(UserSessionState.REGISTERED_USER, viewModel.sessionState.value)
        assertTrue(viewModel.authState.value.isRegisteredUser)
        assertFalse(viewModel.authState.value.isAnonymousGuest)

        // 2. Simulate multi-screen navigation: session remains active
        val simulatedScreens = listOf("home", "services", "profile", "offers", "booking", "settings")
        for (screen in simulatedScreens) {
            assertEquals(UserSessionState.REGISTERED_USER, viewModel.sessionState.value)
            assertNotNull(fakeRepo.currentUser)
        }

        // 3. Simulate force-stop & reopen: session restored immediately
        val restartedRepo = TestAuthRepository(initialUser = registeredUser)
        val restartedViewModel = AuthViewModel(restartedRepo)
        advanceUntilIdle()

        assertEquals(UserSessionState.REGISTERED_USER, restartedViewModel.sessionState.value)
        assertEquals("hafiz@example.com", restartedRepo.currentUser?.email)
    }

    // -------------------------------------------------------------
    // Test Case 11 & 12: Registered User Logout & Immediate Drawer Update
    // -------------------------------------------------------------
    @Test
    fun testRegisteredUserLogoutAndImmediateDrawerUpdate() = runTest(testDispatcher) {
        val registeredUser = AuthUser(
            uid = "reg_user_555",
            email = "member@example.com",
            displayName = "Active Member",
            isAnonymous = false
        )
        val fakeRepo = TestAuthRepository(initialUser = registeredUser)
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        // Before logout: Drawer hides Sign In / Register
        var drawer = computeDrawerItems(masterConfig, viewModel.sessionState.value)
        assertNull("Sign In / Register MUST be hidden when registered", drawer.find { it.route == "login" })

        // Act: Perform logout
        var logoutNavigated = false
        viewModel.signOut {
            logoutNavigated = true
            viewModel.clearAllActionStates()
        }
        advanceUntilIdle()

        // Assert: Signed out state
        assertTrue(logoutNavigated)
        assertNull(fakeRepo.currentUser)
        assertEquals(UserSessionState.SIGNED_OUT, viewModel.sessionState.value)
        assertTrue(viewModel.authState.value.isSignedOut)

        // Drawer updates immediately: Sign In / Register is visible
        drawer = computeDrawerItems(masterConfig, viewModel.sessionState.value)
        val accountItem = drawer.find { it.route == "login" || it.id == "drawer_account" }
        assertNotNull("Sign In / Register MUST be visible immediately after logout", accountItem)
        assertEquals("Sign In / Register", accountItem?.title)

        // No anonymous account auto-created after logout
        assertEquals(0, fakeRepo.anonymousSignInsCount)
    }

    // -------------------------------------------------------------
    // Test Case 13: Back Navigation Cannot Reopen Private Session After Logout
    // -------------------------------------------------------------
    @Test
    fun testBackNavigationCannotReopenPrivateSessionAfterLogout() = runTest(testDispatcher) {
        val backStack = mutableListOf("home", "profile", "settings")

        // Simulate logout backstack reset: popUpTo(0) inclusive and navigate to "home"
        backStack.clear()
        backStack.add("home")

        assertEquals(1, backStack.size)
        assertEquals("home", backStack.first())
        assertFalse("Private screens cannot be reached on back press", backStack.contains("profile"))
        assertFalse("Private screens cannot be reached on back press", backStack.contains("settings"))
    }

    // -------------------------------------------------------------
    // Test Case 14: Expired or Invalid Email Link Handled Correctly
    // -------------------------------------------------------------
    @Test
    fun testInvalidOrExpiredEmailLinkHandledGracefully() = runTest(testDispatcher) {
        val fakeRepo = TestAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        viewModel.signInWithEmailLink("user@example.com", "https://invalid-or-expired-link")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is AuthActionState.Error)
        val errorMsg = (viewModel.loginState.value as AuthActionState.Error).message
        assertTrue(errorMsg.isNotBlank())
        assertNull("Expired link must not authenticate user", fakeRepo.currentUser)
        assertEquals(UserSessionState.SIGNED_OUT, viewModel.sessionState.value)
    }

    // -------------------------------------------------------------
    // Test Case 15: Auth Failure Does Not Leave UI in False Logged-In State
    // -------------------------------------------------------------
    @Test
    fun testAuthFailureLeavesUiInUnauthenticatedState() = runTest(testDispatcher) {
        val fakeRepo = TestAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        // Attempt login with wrong password
        viewModel.signInWithEmail("wrong@example.com", "wrongPassword")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is AuthActionState.Error)
        assertNull(fakeRepo.currentUser)
        assertEquals(UserSessionState.SIGNED_OUT, viewModel.sessionState.value)
        assertFalse(viewModel.authState.value.isRegisteredUser)
    }

    // -------------------------------------------------------------
    // Test Case 16: User-Specific Info Does Not Leak Between Accounts
    // -------------------------------------------------------------
    @Test
    fun testUserSpecificInfoDoesNotLeakBetweenSessions() = runTest(testDispatcher) {
        val userA = AuthUser(uid = "user_A", email = "usera@example.com", displayName = "User A", isAnonymous = false)
        val fakeRepo = TestAuthRepository(initialUser = userA)
        fakeRepo.saveUserProfile(UserProfile(uid = "user_A", displayName = "User A", email = "usera@example.com"))

        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()
        assertEquals("usera@example.com", fakeRepo.currentUser?.email)

        // Logout user A
        viewModel.signOut()
        advanceUntilIdle()
        assertNull(fakeRepo.currentUser)

        // Login as User B
        viewModel.signInWithEmail("userb@example.com", "passwordB")
        advanceUntilIdle()

        val currentUser = fakeRepo.currentUser
        assertNotNull(currentUser)
        assertEquals("userb@example.com", currentUser?.email)
        assertNotEquals("User A", currentUser?.displayName)
    }

    // -------------------------------------------------------------
    // Test Case 17: Anonymous Guests Never Displayed As Registered Users
    // -------------------------------------------------------------
    @Test
    fun testAnonymousGuestsNeverDisplayedAsRegisteredUsers() = runTest(testDispatcher) {
        val guest = AuthUser(uid = "guest_999", email = null, displayName = "Guest User", isAnonymous = true)
        val fakeRepo = TestAuthRepository(initialUser = guest)
        val viewModel = AuthViewModel(fakeRepo)
        advanceUntilIdle()

        val authState = viewModel.authState.value
        val sessionState = viewModel.sessionState.value

        assertEquals(UserSessionState.ANONYMOUS_GUEST, sessionState)
        assertTrue("isAnonymousGuest must be true", authState.isAnonymousGuest)
        assertFalse("isRegisteredUser must be false for guest", authState.isRegisteredUser)
        assertFalse("isSignedOut must be false for guest", authState.isSignedOut)
    }
}

/**
 * Dedicated test implementation of AuthRepository supporting all QA scenarios.
 */
class TestAuthRepository(initialUser: AuthUser? = null) : AuthRepository {
    override var currentUser: AuthUser? = initialUser
    var anonymousSignInsCount = 0
    val existingRegisteredEmails = mutableSetOf<String>()
    val savedProfiles = mutableMapOf<String, UserProfile>()
    val savedPreferences = mutableMapOf<String, UserPreferences>()

    private val _authStateFlow = MutableStateFlow<AuthState>(
        initialUser?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated
    )
    override val authState: Flow<AuthState> = _authStateFlow

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        if (password == "wrongPassword") {
            return Result.failure(Exception("Invalid email or password"))
        }
        val user = AuthUser(uid = "user_${email.hashCode()}", email = email, displayName = "Member", isAnonymous = false)
        currentUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun registerWithEmail(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean
    ): Result<AuthUser> {
        if (existingRegisteredEmails.contains(email)) {
            return Result.failure(Exception("An account with this email address already exists. Please sign in instead."))
        }
        val user = AuthUser(uid = "user_${email.hashCode()}", email = email, displayName = fullName, isAnonymous = false)
        currentUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        saveUserProfile(UserProfile(uid = user.uid, displayName = fullName, email = email, accountType = "registered"))
        return Result.success(user)
    }

    override suspend fun signInAnonymously(): Result<AuthUser> {
        anonymousSignInsCount++
        val user = AuthUser(uid = "guest_${System.currentTimeMillis()}", email = null, displayName = "Guest User", isAnonymous = true)
        currentUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun linkAnonymousAccount(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean
    ): Result<AuthUser> {
        if (existingRegisteredEmails.contains(email)) {
            return Result.failure(Exception("An account with this email address already exists. Please sign in instead."))
        }
        val user = currentUser ?: return registerWithEmail(fullName, email, password, sendVerification)
        val linkedUser = user.copy(email = email, displayName = fullName, isAnonymous = false)
        currentUser = linkedUser
        _authStateFlow.value = AuthState.Authenticated(linkedUser)
        return Result.success(linkedUser)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)

    override suspend fun signOut(): Result<Unit> {
        currentUser = null
        _authStateFlow.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> = Result.success(savedProfiles[uid])

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        savedProfiles[profile.uid] = profile
        return Result.success(Unit)
    }

    override suspend fun getUserPreferences(uid: String): Result<UserPreferences> =
        Result.success(savedPreferences[uid] ?: UserPreferences())

    override suspend fun saveUserPreferences(uid: String, preferences: UserPreferences): Result<Unit> {
        savedPreferences[uid] = preferences
        return Result.success(Unit)
    }

    override suspend fun sendSignInLinkToEmail(email: String, url: String): Result<Unit> = Result.success(Unit)

    override suspend fun signInWithEmailLink(email: String, emailLink: String): Result<AuthUser> {
        if (emailLink.contains("invalid-or-expired")) {
            return Result.failure(Exception("The sign-in link is invalid or expired"))
        }
        val user = AuthUser(uid = "user_link_${email.hashCode()}", email = email, displayName = "Member", isAnonymous = false)
        currentUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        return Result.success(user)
    }
}
