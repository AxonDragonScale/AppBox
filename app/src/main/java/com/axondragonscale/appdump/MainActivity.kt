package com.axondragonscale.appdump

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.appdump.ui.theme.AppDumpTheme
import com.axondragonscale.jest.JestActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppDumpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppList(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class SubApp(
    val name: String,
    val description: String,
    val activityClass: Class<out ComponentActivity>? = null
)

val subApps = listOf(
    SubApp(
        name = "Jest",
        description = "Get your daily dose of humor",
        activityClass = JestActivity::class.java
    ),
    SubApp(
        name = "Wallmatic",
        description = "Automatically changes your wallpaper with time",
        activityClass = null
    ),
    SubApp(
        name = "Topty",
        description = "A lightweight authenticator app",
        activityClass = null
    ),
    SubApp(
        name = "2048",
        description = "The classic 2048 puzzle game",
        activityClass = null
    )
)

@Composable
fun AppList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn(modifier = modifier) {
        items(subApps) { app ->
            SubAppCard(app = app) {
                app.activityClass?.let {
                    context.startActivity(Intent(context, it))
                }
            }
        }
    }
}

@Composable
fun SubAppCard(app: SubApp, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(enabled = app.activityClass != null, onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = app.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppListPreview() {
    AppDumpTheme {
        AppList()
    }
}