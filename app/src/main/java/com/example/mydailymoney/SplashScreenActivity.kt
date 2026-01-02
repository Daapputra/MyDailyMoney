package com.example.mydailymoney

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            // Langsung masuk ke MainActivity tanpa cek first run
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) 
    }
}