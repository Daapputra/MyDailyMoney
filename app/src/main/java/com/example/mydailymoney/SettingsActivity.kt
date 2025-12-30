package com.example.mydailymoney

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnLightMode: MaterialCardView
    private lateinit var btnDarkMode: MaterialCardView
    private lateinit var rgLanguage: RadioGroup
    private lateinit var btnWhatsapp: LinearLayout
    private lateinit var btnTelepon: LinearLayout
    private lateinit var prefs: SharedPreferences

    private val PREF_NAME = "mydailymoney_pref"
    private val KEY_THEME = "theme"
    private val KEY_LANGUAGE = "language"
    private val WA_NUMBER = "6281221070986"
    private val PHONE_NUMBER = "081221070986"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        btnLightMode = findViewById(R.id.btnLightMode)
        btnDarkMode = findViewById(R.id.btnDarkMode)
        rgLanguage = findViewById(R.id.rgLanguage)
        btnWhatsapp = findViewById(R.id.btnWhatsapp)
        btnTelepon = findViewById(R.id.btnTelepon)

        setupThemeButtons()
        setupLanguageRadios()
        setupHelpCenter()
    }

    private fun setupThemeButtons() {
        updateThemeSelection()

        btnLightMode.setOnClickListener {
            saveTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }

        btnDarkMode.setOnClickListener {
            saveTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun saveTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        prefs.edit().putInt(KEY_THEME, themeMode).apply()
        updateThemeSelection()
    }

    private fun updateThemeSelection() {
        when (prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_NO)) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                btnLightMode.strokeWidth = 4 // Active
                btnDarkMode.strokeWidth = 0  // Inactive
            }
            else -> {
                btnLightMode.strokeWidth = 0  // Inactive
                btnDarkMode.strokeWidth = 4 // Active
            }
        }
    }

    private fun setupLanguageRadios() {
        val currentLang = prefs.getString(KEY_LANGUAGE, "id")
        if (currentLang == "id") {
            rgLanguage.check(R.id.rbIndonesia)
        } else {
            rgLanguage.check(R.id.rbEnglish)
        }

        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = if (checkedId == R.id.rbIndonesia) "id" else "en"
            prefs.edit().putString(KEY_LANGUAGE, lang).apply()
            // You might need to recreate the activity to apply language changes fully
            // recreate()
        }
    }

    private fun setupHelpCenter() {
        btnWhatsapp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$WA_NUMBER")))
        }

        btnTelepon.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$PHONE_NUMBER")))
        }
    }
}