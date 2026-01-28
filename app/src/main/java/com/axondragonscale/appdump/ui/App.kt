package com.axondragonscale.appdump.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.appdump.ui.home.HomeScreen
import com.axondragonscale.appdump.ui.theme.AppDumpTheme

@Composable
fun App() {
    AppDumpTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
