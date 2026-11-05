package com.example.invoicemaker.network

import com.example.invoicemaker.BuildConfig
import com.example.invoicemaker.data.SessionStore
import com.example.invoicemaker.data.StoredSession
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

/**
 * Shared calls into the Supabase REST API: config check, error text, and
 * refreshing an expired access token once before giving up.
 */
object SupabaseGateway {
    private val gson = Gson()
    private const val SELECT = "*,line_items(*)"
    private const val RETURN_REPRESENTATION = "return=representation"

    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.startsWith("http") &&
            BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
    }

    fun missingConfigError(): IllegalStateException {
        return IllegalStateException(
            "Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties"
        )
    }

    fun bearer(token: String): String = "Bearer $token"

    fun selectClause(): String = SELECT

    fun returnRepresentation(): String = RETURN_REPRESENTATION

    fun errorMessage(response: Response<*>): String {
        val raw = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        if (raw != null) {
            return try {
                val obj = gson.fromJson(raw, JsonObject::class.java)
                listOf("msg", "error_description", "message", "error", "hint")
                    .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { !it.isJsonNull }?.asString }
                    ?: raw
            } catch (_: Exception) {
                raw
            }
        }
        return "Request failed (${response.code()})"
    }

    fun toSession(response: AuthResponse): StoredSession? {
        val token = response.access_token ?: response.session?.access_token
        val refresh = response.refresh_token ?: response.session?.refresh_token
        val userId = response.user?.id ?: response.session?.user?.id ?: response.id
        val email = response.user?.email ?: response.session?.user?.email ?: response.email
        if (token.isNullOrBlank() || refresh.isNullOrBlank() || userId.isNullOrBlank()) return null
        val now = System.currentTimeMillis() / 1000
        val expiresAt = response.expires_at
            ?: response.session?.expires_at
            ?: response.expires_in?.let { now + it }
            ?: response.session?.expires_in?.let { now + it }
            ?: 0L
        return StoredSession(
            accessToken = token,
            refreshToken = refresh,
            userId = userId,
            email = email.orEmpty(),
            expiresAtEpochSeconds = expiresAt
        )
    }

    /**
     * Runs [block] with a usable access token. On 401, refreshes once and retries.
     */
    suspend fun <T> withAccessToken(
        block: suspend (accessToken: String) -> Response<T>
    ): Result<Response<T>> {
        if (!isConfigured()) return Result.failure(missingConfigError())
        val session = SessionStore.current()
            ?: return Result.failure(IllegalStateException("Not signed in"))

        val token = freshToken(session)
            ?: return Result.failure(IllegalStateException("Session expired. Log in again."))

        val first = block(token)
        if (first.code() != 401) return Result.success(first)

        val refreshed = refresh(SessionStore.current() ?: session) ?: run {
            SessionStore.clear()
            return Result.failure(IllegalStateException("Session expired. Log in again."))
        }
        return Result.success(block(refreshed.accessToken))
    }

    private suspend fun freshToken(session: StoredSession): String? {
        val now = System.currentTimeMillis() / 1000
        if (session.expiresAtEpochSeconds == 0L || session.expiresAtEpochSeconds - 60 > now) {
            return session.accessToken
        }
        return refresh(session)?.accessToken
    }

    private suspend fun refresh(session: StoredSession): StoredSession? {
        return try {
            val response = RetrofitClient.supabaseAuth.refreshGrant(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                grantType = "refresh_token",
                body = RefreshRequest(session.refreshToken)
            )
            if (!response.isSuccessful) {
                null
            } else {
                val updated = response.body()?.let(::toSession) ?: return null
                SessionStore.save(updated)
                updated
            }
        } catch (_: Exception) {
            null
        }
    }
}
