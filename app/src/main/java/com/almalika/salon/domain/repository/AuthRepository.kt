package com.almalika.salon.domain.repository

import com.almalika.salon.domain.model.AuthState
import com.almalika.salon.domain.model.AuthUser
import com.almalika.salon.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: AuthUser?

    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun registerWithEmail(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean = true
    ): Result<AuthUser>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun linkAnonymousAccount(
        fullName: String,
        email: String,
        password: String,
        sendVerification: Boolean = true
    ): Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun signOut(): Result<Unit>

    suspend fun getUserProfile(uid: String): Result<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    suspend fun getUserPreferences(uid: String): Result<com.almalika.salon.domain.model.UserPreferences>
    suspend fun saveUserPreferences(uid: String, preferences: com.almalika.salon.domain.model.UserPreferences): Result<Unit>
    suspend fun sendSignInLinkToEmail(email: String, url: String): Result<Unit>
    suspend fun signInWithEmailLink(email: String, emailLink: String): Result<AuthUser>
}
