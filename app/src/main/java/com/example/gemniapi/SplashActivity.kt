package com.example.gemniapi

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.splash_screen_layout)

        var keepSplashScreen = true
        lifecycleScope.launch {
            delay(2000) // 2 seconds delay
            keepSplashScreen = false
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }


    }
}