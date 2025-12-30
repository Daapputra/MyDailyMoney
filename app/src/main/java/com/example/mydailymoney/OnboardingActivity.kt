package com.example.mydailymoney

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek apakah user sudah pernah buka aplikasi
        val pref = getSharedPreferences("mydailymoney_pref", Context.MODE_PRIVATE)
        if (pref.getBoolean("isFirstRun", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            pref.edit().putBoolean("isFirstRun", true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}