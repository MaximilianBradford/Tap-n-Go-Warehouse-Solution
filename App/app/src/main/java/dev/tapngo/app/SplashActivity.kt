package dev.tapngo.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Simulating a delay for splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Transition to Main Activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the splash screen activity to prevent going back to it
        }, 3000) // Delay of 3 seconds
    }
}