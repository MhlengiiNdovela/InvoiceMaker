package com.example.invoicemaker.data

import android.content.Context
import android.content.SharedPreferences

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val expiresAtEpochSeconds: Long
)

/**
 * Persists the Supabase session on the device so the user stays signed in.
 */
object SessionStore {
    private const val PREFS = "supabase_session"
    private const val ACCESS = "access_token"
    private const val REFRESH = "refresh_token"
    private const val USER_ID = "user_id"
    private const val EMAIL = "email"
    private const val EXPIRES = "expires_at"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun current(): StoredSession? {
        if (!::prefs.isInitialized) return null
        val access = prefs.getString(ACCESS, null) ?: return null
        val refresh = prefs.getString(REFRESH, null) ?: return null
        val userId = prefs.getString(USER_ID, null) ?: return null
        return StoredSession(
            accessToken = access,
            refreshToken = refresh,
            userId = userId,
            email = prefs.getString(EMAIL, "").orEmpty(),
            expiresAtEpochSeconds = prefs.getLong(EXPIRES, 0L)
        )
    }

    fun save(session: StoredSession) {
        prefs.edit()
            .putString(ACCESS, session.accessToken)
            .putString(REFRESH, session.refreshToken)
            .putString(USER_ID, session.userId)
            .putString(EMAIL, session.email)
            .putLong(EXPIRES, session.expiresAtEpochSeconds)
            .apply()
    }

    fun clear() {
        if (::prefs.isInitialized) prefs.edit().clear().apply()
    }
}
