package com.example.invoicemaker.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("InvoiceMakerSettings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_LANGUAGE = "key_language"
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
            val mode = if (value) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, "ZAR") ?: "ZAR"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value).apply()
            val appLocales = LocaleListCompat.forLanguageTags(value)
            AppCompatDelegate.setApplicationLocales(appLocales)
        }
}