package com.axondragonscale.jest

import androidx.compose.ui.window.ComposeUIViewController
import com.axondragonscale.jest.ui.JestApp
import com.axondragonscale.jest.ui.theme.JestTheme

fun MainViewController() = ComposeUIViewController {
    JestTheme {
        JestApp()
    }
}
