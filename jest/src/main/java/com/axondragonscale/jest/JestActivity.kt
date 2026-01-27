package com.axondragonscale.jest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.axondragonscale.jest.ui.JestApp
import com.axondragonscale.jest.ui.theme.JestTheme

class JestActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JestTheme {
                JestApp()
            }
        }

        setupChucker()
    }

    private fun setupChucker() {
        if (/*BuildConfig.DEBUG && */Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED)
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

}

