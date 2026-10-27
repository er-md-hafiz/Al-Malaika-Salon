package com.almalika.salon.data.repository

import com.almalika.salon.domain.model.AuthState
import com.almalika.salon.domain.model.AuthUser
import com.almalika.salon.domain.model.UserProfile
import com.almalika.salon.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AuthRepository {

    private val localGuestFlow = MutableStateFlow<AuthUser?>(null)

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toDomain() ?: localGuestFlow.value

    override val authState: Flow<AuthState> = combine(
        callbackFlow {
            val initialUser = firebaseAuth.currentUser
            if (initialUser != null) {
                trySend(AuthState.Authenticated(initialUser.toDomain(), null))
            } else {
                trySend(AuthState.Unauthenticated)
            }

            val listener = FirebaseAuth.AuthStateListener { auth ->
                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    trySend(AuthState.Unauthenticated)
                } else {
                    val domainUser = firebaseUser.toDomain()
                    trySend(AuthState.Authenticated(domainUser, null))
                }
            }

            firebaseAuth.addAuthStateListener(listener)

            awaitClose {
                firebaseAuth.removeAuthStateListener(listener)
            }
        },
        localGuestFlow
    ) { firebaseState, localGuest ->
        if (firebaseState is AuthState.Authenticated) {
            firebaseState
        } else if (localGuest != null) {
            AuthState.Authenticated(
                user = localGuest,
                profile = UserProfile(
                    uid = localGuest.uid,
                    displayName = "Guest User",
                    email = "",
                    accountType = "anonymous"
                )
            )
        } else {
            firebaseState
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        return try {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            }

            val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val user = authResult.user?.toDomain()
                ?: return Result.failure(IllegalStateException("Sign in failed: user not returned"))

            localGuestFlow.value = null
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun registerWithEmail(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean
    ): Result<AuthUser> {
        return try {
            val trimmedName = fullName.trim()
            val trimmedEmail = email.trim()

            if (trimmedName.isBlank()) {
                return Result.failure(IllegalArgumentException("Full name is required"))
            }
            if (trimmedEmail.isBlank()) {
                return Result.failure(IllegalArgumentException("Email is required"))
            }
            if (password.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            }

            // Check if current session is anonymous and should be linked
            val current = firebaseAuth.currentUser
            if (current != null && current.isAnonymous) {
                return linkAnonymousAccount(trimmedName, trimmedEmail, password, sendVerification)
            }

            val authResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Account creation failed: user not returned"))

            // Update user profile display name in Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Optional email verification
            if (sendVerification) {
                try {
                    firebaseUser.sendEmailVerification().await()
                } catch (_: Exception) {
                    // Do not fail registration if verification email dispatch fails
                }
            }

            val domainUser = firebaseUser.toDomain()

            // Persist profile to Realtime Database
            val profile = UserProfile(
                uid = domainUser.uid,
                displayName = trimmedName,
                email = trimmedEmail,
                accountType = "registered",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveUserProfile(profile)

            localGuestFlow.value = null
            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signInAnonymously(): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Guest login failed"))

            localGuestFlow.value = null
            val domainUser = firebaseUser.toDomain()

            // Record initial guest profile in Realtime Database
            val guestProfile = UserProfile(
                uid = domainUser.uid,
                displayName = "Guest User",
                email = "",
                accountType = "anonymous",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveUserProfile(guestProfile)

            Result.success(domainUser)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            val isRestricted = msg.contains("restricted to administrators", ignoreCase = true) ||
                    msg.contains("ADMIN_ONLY_OPERATION", ignoreCase = true) ||
                    (e is FirebaseAuthException && (e.errorCode == "ERROR_ADMIN_RESTRICTED_OPERATION" || e.errorCode == "admin-restricted-operation"))

            if (isRestricted) {
                // Graceful fallback: Anonymous provider is not yet enabled in Firebase Console.
                // Enable local guest browsing session so the user can immediately browse the salon app.
                val localGuest = AuthUser(
                    uid = "guest_local_${System.currentTimeMillis()}",
                    email = null,
                    displayName = "Guest User",
                    isAnonymous = true,
                    isEmailVerified = false
                )
                localGuestFlow.value = localGuest
                Result.success(localGuest)
            } else {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun linkAnonymousAccount(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean
    ): Result<AuthUser> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null || localGuestFlow.value != null) {
                localGuestFlow.value = null
                return registerWithEmail(fullName, email, password, sendVerification)
            }

            val trimmedName = fullName.trim()
            val trimmedEmail = email.trim()
            val credential = EmailAuthProvider.getCredential(trimmedEmail, password)

            val linkResult = try {
                user.linkWithCredential(credential).await()
            } catch (collision: FirebaseAuthUserCollisionException) {
                // Email belongs to an existing account. Preserve guest data and advise user to sign in.
                return Result.failure(Exception("An account with this email address already exists. Please sign in instead.", collision))
            }
            val linkedUser = linkResult.user
                ?: return Result.failure(IllegalStateException("Account linking failed"))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
            linkedUser.updateProfile(profileUpdates).await()

            if (sendVerification) {
                try {
                    linkedUser.sendEmailVerification().await()
                } catch (_: Exception) {
                    // Non-fatal
                }
            }

            val domainUser = linkedUser.toDomain()

            val updatedProfile = UserProfile(
                uid = domainUser.uid,
                displayName = trimmedName,
                email = trimmedEmail,
                accountType = "registered",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveUserProfile(updatedProfile)

            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val trimmed = email.trim()
            if (trimmed.isBlank()) {
                return Result.failure(IllegalArgumentException("Email address is required"))
            }
            firebaseAuth.sendPasswordResetEmail(trimmed).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(IllegalStateException("No authenticated user"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            localGuestFlow.value = null
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        if (uid.startsWith("guest_local_")) {
            return Result.success(
                UserProfile(
                    uid = uid,
                    displayName = "Guest User",
                    email = "",
                    accountType = "anonymous"
                )
            )
        }
        return try {
            val userRef = firebaseDatabase.reference.child("users").child(uid)
            val profileSnapshot = userRef.child("profile").get().await()

            if (!profileSnapshot.exists()) {
                return Result.success(null)
            }

            val displayName = profileSnapshot.child("displayName").getValue(String::class.java).orEmpty()
            val email = profileSnapshot.child("email").getValue(String::class.java).orEmpty()
            val accountType = profileSnapshot.child("accountType").getValue(String::class.java).orEmpty().ifBlank { "registered" }
            val createdAt = profileSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
            val updatedAt = profileSnapshot.child("updatedAt").getValue(Long::class.java) ?: 0L

            val prefSnapshot = userRef.child("preferences").get().await()
            val preferences = if (prefSnapshot.exists()) {
                com.almalika.salon.domain.model.UserPreferences(
                    darkMode = prefSnapshot.child("darkMode").getValue(Boolean::class.java) ?: true,
                    notifications = prefSnapshot.child("notifications").getValue(Boolean::class.java) ?: true
                )
            } else {
                com.almalika.salon.domain.model.UserPreferences()
            }

            Result.success(
                UserProfile(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    accountType = accountType,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    preferences = preferences
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        if (profile.uid.startsWith("guest_local_")) {
            return Result.success(Unit)
        }
        return try {
            val userRef = firebaseDatabase.reference.child("users").child(profile.uid)
            val profileMap = mapOf(
                "displayName" to profile.displayName,
                "email" to profile.email,
                "accountType" to profile.accountType,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt
            )
            userRef.child("profile").setValue(profileMap).await()

            val prefMap = mapOf(
                "darkMode" to profile.preferences.darkMode,
                "notifications" to profile.preferences.notifications
            )
            userRef.child("preferences").setValue(prefMap).await()

            Result.success(Unit)
        } catch (e: Exception) {
            // Profile write failures in RTDB should not crash the app, but recorded as failure
            Result.failure(e)
        }
    }

    override suspend fun getUserPreferences(uid: String): Result<com.almalika.salon.domain.model.UserPreferences> {
        if (uid.startsWith("guest_local_")) {
            return Result.success(com.almalika.salon.domain.model.UserPreferences())
        }
        return try {
            val snapshot = firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("preferences")
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.success(com.almalika.salon.domain.model.UserPreferences())
            }

            val darkMode = snapshot.child("darkMode").getValue(Boolean::class.java) ?: true
            val notifications = snapshot.child("notifications").getValue(Boolean::class.java) ?: true
            Result.success(com.almalika.salon.domain.model.UserPreferences(darkMode = darkMode, notifications = notifications))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserPreferences(uid: String, preferences: com.almalika.salon.domain.model.UserPreferences): Result<Unit> {
        if (uid.startsWith("guest_local_")) {
            return Result.success(Unit)
        }
        return try {
            val prefMap = mapOf(
                "darkMode" to preferences.darkMode,
                "notifications" to preferences.notifications
            )
            firebaseDatabase.reference
                .child("users")
                .child(uid)
                .child("preferences")
                .setValue(prefMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendSignInLinkToEmail(email: String, url: String): Result<Unit> {
        return try {
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                .setHandleCodeInApp(true)
                .setAndroidPackageName("com.almalika.salon", true, "1")
                .build()

            firebaseAuth.sendSignInLinkToEmail(email.trim(), actionCodeSettings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signInWithEmailLink(email: String, emailLink: String): Result<AuthUser> {
        return try {
            val trimmedEmail = email.trim()
            val currentUser = firebaseAuth.currentUser

            val firebaseUser = if (currentUser?.isAnonymous == true) {
                val credential = EmailAuthProvider.getCredentialWithLink(trimmedEmail, emailLink)
                try {
                    val linkResult = currentUser.linkWithCredential(credential).await()
                    linkResult.user ?: throw IllegalStateException("Email link credential linking failed")
                } catch (collision: FirebaseAuthUserCollisionException) {
                    val guestPreferences = getUserPreferences(currentUser.uid).getOrNull()
                    val authResult = firebaseAuth.signInWithEmailLink(trimmedEmail, emailLink).await()
                    val existingUser = authResult.user ?: throw IllegalStateException("Email link sign in failed")
                    if (guestPreferences != null) {
                        try {
                            saveUserPreferences(existingUser.uid, guestPreferences)
                        } catch (_: Exception) {}
                    }
                    existingUser
                }
            } else {
                localGuestFlow.value = null
                val authResult = firebaseAuth.signInWithEmailLink(trimmedEmail, emailLink).await()
                authResult.user ?: throw IllegalStateException("Email link sign in failed")
            }

            val domainUser = firebaseUser.toDomain()

            val existingProfile = getUserProfile(domainUser.uid).getOrNull()
            val profile = UserProfile(
                uid = domainUser.uid,
                displayName = existingProfile?.displayName?.ifBlank { domainUser.displayName ?: "Al-Malaika Member" } ?: "Al-Malaika Member",
                email = trimmedEmail,
                accountType = "email_link",
                createdAt = existingProfile?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            try {
                saveUserProfile(profile)
            } catch (_: Exception) {
                // Non-fatal profile write error
            }

            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    private fun FirebaseUser.toDomain(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            isAnonymous = isAnonymous,
            isEmailVerified = isEmailVerified,
            photoUrl = photoUrl?.toString()
        )
    }

    private fun mapAuthException(e: Exception): Exception {
        val userFriendlyMessage = when {
            e is FirebaseAuthInvalidUserException ->
                "No account exists with this email address."
            e is FirebaseAuthInvalidCredentialsException ->
                "Invalid credentials. Please check your email and password."
            e is FirebaseAuthUserCollisionException ->
                "An account with this email address already exists. Please sign in instead."
            e is FirebaseAuthWeakPasswordException ->
                "The password is too weak. Please use at least 6 characters."
            e is FirebaseNetworkException ->
                "Network connection failed. Please check your internet connection."
            e is FirebaseTooManyRequestsException ->
                "Too many attempts. Access is temporarily disabled. Please try again later."
            e is FirebaseAuthException && (e.errorCode == "ERROR_ADMIN_RESTRICTED_OPERATION" || e.errorCode == "admin-restricted-operation") ->
                "Anonymous sign-in is disabled in your Firebase Console. Please enable 'Anonymous' in Firebase Authentication > Sign-in method, or sign up with email."
            e.message?.contains("restricted to administrators", ignoreCase = true) == true ->
                "Anonymous sign-in is disabled in your Firebase Console. Please enable 'Anonymous' in Firebase Authentication > Sign-in method, or sign up with email."
            else ->
                e.message?.takeIf { it.isNotBlank() } ?: "Authentication error. Please try again."
        }
        return Exception(userFriendlyMessage, e)
    }
}
