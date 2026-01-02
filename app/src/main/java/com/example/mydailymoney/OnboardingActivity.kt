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
        // Saya ganti key ke "onboarding_done_v3" agar halaman ini muncul lagi untuk Anda cek
        if (pref.getBoolean("onboarding_done_v3", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_onboarding)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            // Simpan status bahwa onboarding sudah selesai
            pref.edit().putBoolean("onboarding_done_v3", true).apply()
            
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}