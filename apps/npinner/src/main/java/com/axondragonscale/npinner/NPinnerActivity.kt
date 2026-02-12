package com.axondragonscale.npinner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.axondragonscale.npinner.core.NPinnerNotificationMonitor
import com.axondragonscale.npinner.model.DarkModeConfig
import com.axondragonscale.npinner.repository.AppPrefsRepository
import com.axondragonscale.npinner.ui.NPinnerApp
import com.axondragonscale.npinner.ui.theme.NPinnerTheme
import com.axondragonscale.npinner.work.monitor.MonitorRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NPinnerActivity : ComponentActivity() {

    private val notificationMonitor: NPinnerNotificationMonitor by inject()
    private val appPrefsRepository: AppPrefsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModeConfig by appPrefsRepository.darkModeConfigFlow
                .collectAsStateWithLifecycle(initialValue = DarkModeConfig.AUTO)
            NPinnerTheme(darkTheme = isDarkMode(darkModeConfig = darkModeConfig)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NPinnerApp()
                }
            }
        }

        GlobalScope.launch {
            notificationMonitor.ensurePinnedNotificationVisibility()
            WorkManager.getInstance(this@NPinnerActivity)
                .enqueueUniquePeriodicWork(
                    MonitorRequest.MONITOR_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    MonitorRequest.getRequest(),
                )
        }
    }

}

@Composable
fun isDarkMode(darkModeConfig: DarkModeConfig) = when (darkModeConfig) {
    DarkModeConfig.AUTO -> isSystemInDarkTheme()
    DarkModeConfig.ON -> true
    DarkModeConfig.OFF -> false
}
