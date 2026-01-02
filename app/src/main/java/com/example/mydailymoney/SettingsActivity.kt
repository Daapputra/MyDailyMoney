package com.example.mydailymoney

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: SwitchMaterial

    private lateinit var prefs: SharedPreferences

    private val PREF_NAME = "mydailymoney_pref"
    private val KEY_THEME = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Initialize theme view
        switchDarkMode = findViewById(R.id.switchDarkMode)

        setupThemeSwitch()
    }

    private fun setupThemeSwitch() {
        // Set initial state
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        switchDarkMode.isChecked = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveTheme(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                saveTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun saveTheme(themeMode: Int) {
        if (AppCompatDelegate.getDefaultNightMode() != themeMode) {
            AppCompatDelegate.setDefaultNightMode(themeMode)
            prefs.edit().putInt(KEY_THEME, themeMode).apply()
        }
    }
}
