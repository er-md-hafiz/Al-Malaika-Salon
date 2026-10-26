package com.almalika.salon.auth

import com.almalika.salon.data.mapper.toDomain
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.domain.model.AuthState
import com.almalika.salon.domain.model.AuthUser
import com.almalika.salon.domain.model.UserPreferences
import com.almalika.salon.domain.model.UserProfile
import com.almalika.salon.domain.repository.AuthRepository
import com.almalika.salon.presentation.auth.AuthActionState
import com.almalika.salon.presentation.auth.AuthViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AuthValidationAndConfigTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test fallback menu json contains and maps authentication configuration`() {
        val fallbackFile = File("src/main/assets/config/menu_fallback.json")
        assertTrue("Fallback menu json must exist", fallbackFile.exists())
        val json = fallbackFile.readText()

        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(json)
        assertNotNull(dto)

        val master = dto!!.toDomain()

        // 1. Login Config Verification
        assertNotNull(master.login)
        assertTrue(master.login.enabled)
        assertEquals("firebase_email_password", master.login.authMode)
        assertTrue(master.login.allowGuest)
        assertEquals("Welcome Back", master.login.title)
        assertTrue(master.login.showEmailLogin)
        assertTrue(master.login.showRegister)
        assertTrue(master.login.showForgotPassword)
        assertTrue(master.login.showGuestButton)
        assertEquals("Continue as Guest", master.login.guestButtonText)
        assertEquals("register", master.login.registerDestination)
        assertEquals("forgot_password", master.login.forgotPasswordDestination)
        assertEquals("home", master.login.successDestination)

        // 2. Register Config Verification
        assertNotNull(master.register)
        assertTrue(master.register.enabled)
        assertEquals("Create Your Account", master.register.title)
        assertTrue(master.register.showFullName)
        assertTrue(master.register.showEmail)
        assertTrue(master.register.showPassword)
        assertTrue(master.register.showConfirmPassword)
        assertTrue(master.register.requireTermsAcceptance)
        assertTrue(master.register.sendEmailVerification)
        assertEquals("login", master.register.loginDestination)
        assertEquals("home", master.register.successDestination)

        // 3. Forgot Password Config Verification
        assertNotNull(master.forgotPassword)
        assertTrue(master.forgotPassword.enabled)
        assertEquals("Reset Your Password", master.forgotPassword.title)
        assertTrue(master.forgotPassword.emailRequired)
        assertEquals("login", master.forgotPassword.loginDestination)

        // 4. Authentication Master Flags
        assertNotNull(master.authentication)
        assertEquals("firebase", master.authentication.provider)
        assertTrue(master.authentication.emailPasswordEnabled)
        assertTrue(master.authentication.anonymousEnabled)
        assertTrue(master.authentication.guestAccountLinkingEnabled)
        assertTrue(master.authentication.persistSession)

        // 5. Drawer contains Account screen
        val accountDrawerItem = master.drawer.find { it.id == "drawer_account" || it.route == "account" }
        assertNotNull("Drawer should contain Account item", accountDrawerItem)
        assertEquals("My Account", accountDrawerItem?.title)
    }

    @Test
    fun `test login validation rejects invalid emails and short passwords`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        // Invalid email
        viewModel.signInWithEmail("notanemail", "password123")
        assertTrue(viewModel.loginState.value is AuthActionState.Error)
        assertEquals("Please enter a valid email address", (viewModel.loginState.value as AuthActionState.Error).message)

        // Short password (< 6 characters)
        viewModel.signInWithEmail("test@example.com", "12345")
        assertTrue(viewModel.loginState.value is AuthActionState.Error)
        assertEquals("Password must be at least 6 characters", (viewModel.loginState.value as AuthActionState.Error).message)

        // Valid credentials should call repository
        viewModel.signInWithEmail("test@example.com", "validPass123")
        advanceUntilIdle()
        assertTrue(viewModel.loginState.value is AuthActionState.Success)
    }

    @Test
    fun `test registration validation rejects mismatch passwords and missing terms`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        // Empty full name
        viewModel.register("", "test@example.com", "password123", "password123", acceptTerms = true, requireTerms = true)
        assertTrue(viewModel.registerState.value is AuthActionState.Error)
        assertEquals("Please enter your full name", (viewModel.registerState.value as AuthActionState.Error).message)

        // Password mismatch
        viewModel.register("John Doe", "test@example.com", "password123", "password999", acceptTerms = true, requireTerms = true)
        assertTrue(viewModel.registerState.value is AuthActionState.Error)
        assertEquals("Passwords do not match", (viewModel.registerState.value as AuthActionState.Error).message)

        // Terms not accepted
        viewModel.register("John Doe", "test@example.com", "password123", "password123", acceptTerms = false, requireTerms = true)
        assertTrue(viewModel.registerState.value is AuthActionState.Error)
        assertEquals("Please accept Terms and Conditions to proceed", (viewModel.registerState.value as AuthActionState.Error).message)

        // Valid registration
        viewModel.register("John Doe", "test@example.com", "password123", "password123", acceptTerms = true, requireTerms = true)
        advanceUntilIdle()
        assertTrue(viewModel.registerState.value is AuthActionState.Success)
        assertEquals("John Doe", fakeRepo.lastCreatedUser?.displayName)
    }

    @Test
    fun `test forgot password validation rejects invalid email`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        viewModel.sendPasswordReset("bademail")
        assertTrue(viewModel.forgotPasswordState.value is AuthActionState.Error)

        viewModel.sendPasswordReset("user@example.com")
        advanceUntilIdle()
        assertTrue(viewModel.forgotPasswordState.value is AuthActionState.Success)
        assertEquals("user@example.com", fakeRepo.lastResetEmail)
    }

    @Test
    fun `test anonymous guest sign in and linking to permanent account`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        // 1. Guest sign in
        viewModel.signInAnonymously()
        advanceUntilIdle()
        assertTrue(viewModel.loginState.value is AuthActionState.Success)
        val guestUser = fakeRepo.currentUser
        assertNotNull(guestUser)
        assertTrue(guestUser!!.isAnonymous)

        // 2. Linking guest to permanent email account
        viewModel.register("Jane Doe", "jane@example.com", "secret123", "secret123", acceptTerms = true, requireTerms = false)
        advanceUntilIdle()
        assertTrue(viewModel.registerState.value is AuthActionState.Success)
        assertFalse("Account should no longer be anonymous after linking", fakeRepo.currentUser!!.isAnonymous)
        assertEquals("jane@example.com", fakeRepo.currentUser!!.email)
        assertEquals("Jane Doe", fakeRepo.currentUser!!.displayName)
    }

    @Test
    fun `test passwordless email link authentication and profile preferences persistence`() = runTest(testDispatcher) {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        // Send sign-in link
        viewModel.sendSignInLinkToEmail("customer@example.com", "https://al-malika-salon.firebaseapp.com/finishSignIn")
        advanceUntilIdle()
        assertTrue(viewModel.loginState.value is AuthActionState.Success)

        // Sign in with link
        viewModel.signInWithEmailLink("customer@example.com", "https://al-malika-salon.firebaseapp.com/finishSignIn?apiKey=xyz")
        advanceUntilIdle()
        assertTrue(viewModel.loginState.value is AuthActionState.Success)
        assertEquals("customer@example.com", fakeRepo.currentUser?.email)

        // User profile and preferences saved
        val profile = fakeRepo.getUserProfile(fakeRepo.currentUser!!.uid).getOrNull()
        assertNotNull(profile)
        assertEquals("customer@example.com", profile?.email)
        assertEquals("email_link", profile?.accountType)
        assertTrue(profile?.preferences?.darkMode == true)
    }
}

/**
 * Test double for AuthRepository simulating Firebase Auth & Realtime Database behavior.
 */
class FakeAuthRepository : AuthRepository {
    override var currentUser: AuthUser? = null
    var lastCreatedUser: AuthUser? = null
    var lastResetEmail: String? = null
    val savedProfiles = mutableMapOf<String, UserProfile>()
    val savedPreferences = mutableMapOf<String, UserPreferences>()
    private val _authStateFlow = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    override val authState: Flow<AuthState> = _authStateFlow

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val user = AuthUser(uid = "user_123", email = email, displayName = "Test Member", isAnonymous = false, isEmailVerified = true)
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
        val uid = "new_user_${System.currentTimeMillis()}"
        val user = AuthUser(uid = uid, email = email, displayName = fullName, isAnonymous = false, isEmailVerified = false)
        currentUser = user
        lastCreatedUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        saveUserProfile(UserProfile(uid = uid, displayName = fullName, email = email, accountType = "registered"))
        return Result.success(user)
    }

    override suspend fun signInAnonymously(): Result<AuthUser> {
        val user = AuthUser(uid = "guest_${System.currentTimeMillis()}", email = null, displayName = "Guest User", isAnonymous = true, isEmailVerified = false)
        currentUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        saveUserProfile(UserProfile(uid = user.uid, displayName = "Guest User", email = "", accountType = "anonymous"))
        return Result.success(user)
    }

    override suspend fun linkAnonymousAccount(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean
    ): Result<AuthUser> {
        val current = currentUser ?: return registerWithEmail(fullName, email, password, sendVerification)
        val linkedUser = current.copy(email = email, displayName = fullName, isAnonymous = false)
        currentUser = linkedUser
        _authStateFlow.value = AuthState.Authenticated(linkedUser)
        saveUserProfile(UserProfile(uid = linkedUser.uid, displayName = fullName, email = email, accountType = "registered"))
        return Result.success(linkedUser)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        lastResetEmail = email
        return Result.success(Unit)
    }

    override suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)

    override suspend fun signOut(): Result<Unit> {
        currentUser = null
        _authStateFlow.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> = Result.success(savedProfiles[uid])

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        savedProfiles[profile.uid] = profile
        savedPreferences[profile.uid] = profile.preferences
        return Result.success(Unit)
    }

    override suspend fun getUserPreferences(uid: String): Result<UserPreferences> {
        return Result.success(savedPreferences[uid] ?: UserPreferences())
    }

    override suspend fun saveUserPreferences(uid: String, preferences: UserPreferences): Result<Unit> {
        savedPreferences[uid] = preferences
        return Result.success(Unit)
    }

    override suspend fun sendSignInLinkToEmail(email: String, url: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun signInWithEmailLink(email: String, emailLink: String): Result<AuthUser> {
        val uid = currentUser?.takeIf { it.isAnonymous }?.uid ?: "user_email_link_${System.currentTimeMillis()}"
        val user = AuthUser(uid = uid, email = email, displayName = "Email Link Member", isAnonymous = false, isEmailVerified = true)
        currentUser = user
        _authStateFlow.value = AuthState.Authenticated(user)
        saveUserProfile(UserProfile(uid = uid, displayName = "Email Link Member", email = email, accountType = "email_link"))
        return Result.success(user)
    }
}
