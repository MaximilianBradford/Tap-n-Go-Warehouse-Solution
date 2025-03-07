package dev.tapngo.app.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dev.tapngo.app.R

class ActivitySettings : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        val themeSwitch: Switch = findViewById(R.id.switch_theme)
        val nfcSwitch: Switch = findViewById(R.id.switch_nfc)
        val notificationsSwitch: Switch = findViewById(R.id.switch_notifications)

        // Load saved preferences
        themeSwitch.isChecked = sharedPreferences.getBoolean("dark_mode", false)
        nfcSwitch.isChecked = sharedPreferences.getBoolean("nfc_enabled", true)
        notificationsSwitch.isChecked = sharedPreferences.getBoolean("notifications_enabled", true)

        // Theme Toggle
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // NFC Toggle
        nfcSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("nfc_enabled", isChecked)
            editor.apply()
            Toast.makeText(this, if (isChecked) "NFC Enabled" else "NFC Disabled", Toast.LENGTH_SHORT).show()
        }

        // Notifications Toggle
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("notifications_enabled", isChecked)
            editor.apply()
            Toast.makeText(this, if (isChecked) "Notifications Enabled" else "Notifications Disabled", Toast.LENGTH_SHORT).show()
        }
    }
}
