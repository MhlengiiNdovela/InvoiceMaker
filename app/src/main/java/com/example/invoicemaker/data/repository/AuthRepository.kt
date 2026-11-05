package com.example.invoicemaker.data.repository

import com.example.invoicemaker.BuildConfig
import com.example.invoicemaker.data.SessionStore
import com.example.invoicemaker.data.StoredSession
import com.example.invoicemaker.network.AuthRequest
import com.example.invoicemaker.network.AuthResponse
import com.example.invoicemaker.network.RetrofitClient
import com.example.invoicemaker.network.SupabaseGateway

data class SignedInUser(
    val uid: String,
    val email: String
)

/**
 * Email and password authentication against the Supabase Auth REST API.
 * The session is stored on the device so the user stays signed in.
 */
class AuthRepository {

    val currentUser: SignedInUser?
        get() = SessionStore.current()?.let { SignedInUser(it.userId, it.email) }

    suspend fun register(email: String, password: String): Result<SignedInUser> {
        if (!SupabaseGateway.isConfigured()) return Result.failure(SupabaseGateway.missingConfigError())
        return try {
            val response = RetrofitClient.supabaseAuth.signUp(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                body = AuthRequest(email, password)
            )
            if (!response.isSuccessful) {
                Result.failure(Exception(SupabaseGateway.errorMessage(response)))
            } else {
                saveSession(response.body())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<SignedInUser> {
        if (!SupabaseGateway.isConfigured()) return Result.failure(SupabaseGateway.missingConfigError())
        return try {
            val response = RetrofitClient.supabaseAuth.passwordGrant(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                grantType = "password",
                body = AuthRequest(email, password)
            )
            if (!response.isSuccessful) {
                Result.failure(Exception(SupabaseGateway.errorMessage(response)))
            } else {
                saveSession(response.body())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Drops the local session immediately. Returns it so the caller can revoke
     * the access token on the server without blocking navigation.
     */
    fun beginLogout(): StoredSession? {
        val session = SessionStore.current()
        SessionStore.clear()
        return session
    }

    suspend fun revoke(session: StoredSession) {
        if (!SupabaseGateway.isConfigured()) return
        try {
            RetrofitClient.supabaseAuth.logout(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                bearer = SupabaseGateway.bearer(session.accessToken)
            )
        } catch (_: Exception) {
        }
    }

    private fun saveSession(body: AuthResponse?): Result<SignedInUser> {
        val session = body?.let(SupabaseGateway::toSession)
            ?: return Result.failure(
                Exception(
                    "No session was returned. In Supabase, turn off Authentication, Providers, Email, Confirm email, then try again."
                )
            )
        SessionStore.save(session)
        return Result.success(SignedInUser(session.userId, session.email))
    }
}
