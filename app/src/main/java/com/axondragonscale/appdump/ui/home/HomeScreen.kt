package com.axondragonscale.appdump.ui.home

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.appdump.ui.theme.AppDumpTheme
import com.axondragonscale.jest.JestActivity
import com.axondragonscale.wallmatic.WallmaticActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("AppDump") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(subApps) { app ->
                SubAppCard(app = app) {
                    app.onClick(context)
                }
            }
        }
    }
}

@Composable
private fun SubAppCard(app: SubApp, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = app.enabled
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = app.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class SubApp(
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val onClick: (Context) -> Unit = {}
)

private val subApps = listOf(
    SubApp(
        name = "Jest",
        description = "Get your daily dose of humor",
        onClick = { JestActivity.start(it) }
    ),
    SubApp(
        name = "Wallmatic",
        description = "Automatically changes your wallpaper with time",
        onClick = { WallmaticActivity.start(it) }
    ),
    SubApp(
        name = "Topty",
        description = "A lightweight authenticator app",
        enabled = false,
    ),
    SubApp(
        name = "2048",
        description = "The classic 2048 puzzle game",
        enabled = false,
    )
)

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppDumpTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HomeScreen()
        }
    }
}
