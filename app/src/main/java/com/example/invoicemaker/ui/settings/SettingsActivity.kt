package com.example.invoicemaker.ui.settings

import com.example.invoicemaker.R
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.invoicemaker.util.LocaleHelper
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.content.edit


class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var spinnerCurrency: Spinner
    private lateinit var rgLanguage: RadioGroup
    private lateinit var rbEnglish: RadioButton
    private lateinit var rbAfrikaans: RadioButton

    private val currencies = arrayOf("ZAR (R)", "USD ($)", "EUR (€)", "GBP (£)")
    private val currencyCodes = arrayOf("ZAR", "USD", "EUR", "GBP")

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("InvoiceMakerSettings", MODE_PRIVATE)

        // Initialize Views
        switchDarkMode = findViewById(R.id.switchDarkMode)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        rgLanguage = findViewById(R.id.rgLanguage)
        rbEnglish = findViewById(R.id.rbEnglish)
        rbAfrikaans = findViewById(R.id.rbAfrikaans)

        // -----------------------------
        // 1. Theme Setup
        // -----------------------------
        val isDarkMode = prefs.getBoolean("KEY_DARK_MODE", false)
        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("KEY_DARK_MODE", isChecked) }
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // -----------------------------
        // 2. Currency Setup
        // -----------------------------
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter

        val savedCurrency = prefs.getString("KEY_CURRENCY", "ZAR")
        val currencyIndex = currencyCodes.indexOf(savedCurrency).takeIf { it >= 0 } ?: 0
        spinnerCurrency.setSelection(currencyIndex)

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCode = currencyCodes[position]
                prefs.edit { putString("KEY_CURRENCY", selectedCode) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // -----------------------------
        // 3. Language Setup
        // -----------------------------
        val currentLang = LocaleHelper.getSavedLanguage(this)
        if (currentLang == "af") {
            rbAfrikaans.isChecked = true
        } else {
            rbEnglish.isChecked = true
        }

        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val selectedLang = if (checkedId == R.id.rbAfrikaans) "af" else "en"
            if (selectedLang != LocaleHelper.getSavedLanguage(this)) {
                LocaleHelper.setLocale(this, selectedLang)

                // Recreate activity to apply updated language resources immediately
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }
}
