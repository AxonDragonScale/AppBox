package com.axondragonscale.appbox.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.axondragonscale.appbox.BuildConfig
import com.axondragonscale.appbox.data.AppThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") }
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
            item {
                AppearanceSection(
                    themeMode = uiState.themeMode,
                    isDynamicTheme = uiState.isDynamicTheme,
                    onThemeModeChange = viewModel::setThemeMode,
                    onDynamicThemeChange = viewModel::setDynamicTheme
                )
            }

            item {
                DevToolsSection(
                    onClearData = { viewModel.clearData(context) }
                )
            }

            item {
                AboutSection()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSection(
    themeMode: AppThemeMode,
    isDynamicTheme: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onDynamicThemeChange: (Boolean) -> Unit
) {
    SettingsSection("Appearance") {
        // Theme Mode
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Theme", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(AppThemeMode.LIGHT, AppThemeMode.SYSTEM, AppThemeMode.DARK)
                options.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = { onThemeModeChange(mode) },
                        selected = themeMode == mode,
                        label = {
                            Text(
                                when (mode) {
                                    AppThemeMode.LIGHT -> "Light"
                                    AppThemeMode.DARK -> "Dark"
                                    AppThemeMode.SYSTEM -> "Auto"
                                }
                            )
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Dynamic Theme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDynamicThemeChange(!isDynamicTheme) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dynamic Theme", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDynamicTheme,
                onCheckedChange = null,
                enabled = true,
            )
        }
    }
}

@Composable
fun DevToolsSection(
    onClearData: () -> Unit
) {
    val context = LocalContext.current
    var permissions by remember { mutableStateOf<List<PermissionInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        permissions = getPermissions(context)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissions = getPermissions(context)
    }

    SettingsSection("Dev Tools") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClearData)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Clear App Data", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Permissions", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (permissions.isEmpty()) {
                Text("No dangerous permissions requested.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                permissions.forEach { perm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(perm.name.substringAfterLast("."), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(
                            if (perm.granted) "Granted" else "Denied",
                            color = if (perm.granted) Color.Green else Color.Red,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                val missingPermissions = permissions.filter { !it.granted }.map { it.name }.toTypedArray()
                if (missingPermissions.isNotEmpty()) {
                    Button(
                        onClick = { launcher.launch(missingPermissions) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Request All Missing")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSection() {
    SettingsSection("About") {
        InfoRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        InfoRow("Developer", "Ronak Harkhani")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        LinkRow("GitHub Profile", "https://github.com/AxonDragonScale/")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        LinkRow("Source Code", "https://github.com/AxonDragonScale/AppBox")
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}

@Composable
fun LinkRow(label: String, url: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text("Open ↗", color = MaterialTheme.colorScheme.primary)
    }
}

data class PermissionInfo(val name: String, val granted: Boolean)

fun getPermissions(context: Context): List<PermissionInfo> {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
    } else {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    }

    val declaredPermissions = packageInfo.requestedPermissions ?: return emptyList()

    return declaredPermissions.mapNotNull { permission ->
        try {
            val permissionInfo = context.packageManager.getPermissionInfo(permission, 0)
            // Filter for dangerous permissions only (runtime permissions)
            @Suppress("DEPRECATION")
            if ((permissionInfo.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_DANGEROUS) != 0) {
                PermissionInfo(
                    name = permission,
                    granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                )
            } else {
                null
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
