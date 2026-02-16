package com.axondragonscale.limitless.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.limitless.BuildConfig
import com.axondragonscale.limitless.Link
import com.axondragonscale.limitless.R
import com.axondragonscale.limitless.model.UIMode
import com.axondragonscale.limitless.ui.Route
import com.axondragonscale.limitless.ui.common.AppBar
import com.axondragonscale.limitless.ui.common.AppBarItem
import com.axondragonscale.limitless.ui.common.Dropdown
import com.axondragonscale.limitless.ui.common.DropdownItem
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.util.thenIf
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 01/11/24
 */

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: SettingsVM = koinViewModel()
    val uiState = vm.uiState.collectAsStateWithLifecycle()

    Settings(
        modifier = modifier,
        uiState = uiState.value,
        onEvent = { event ->
            when (event) {
                SettingsUiEvent.NavigateBack -> navController.popBackStack()
                SettingsUiEvent.NavigateToDevTools -> navController.navigate(Route.DevTools)

                else -> vm.onEvent(event)
            }
        }
    )
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
) = Surface(modifier.systemBarsPadding()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))

        val scrollState = rememberScrollState()
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            AppearanceSection(
                modifier = Modifier.padding(top = 16.dp),
                uiMode = uiState.uiMode,
                dynamicTheme = uiState.dynamicTheme,
                onUiModeUpdated = { onEvent(SettingsUiEvent.UIModeUpdated(it)) },
                onDynamicThemeToggled = { onEvent(SettingsUiEvent.DynamicThemeToggled(it)) }
            )

            BehaviorSection(modifier = Modifier.padding(top = 16.dp))

            AboutSection(modifier = Modifier.padding(top = 16.dp))

            DevToolsSection(
                modifier = Modifier.padding(top = 16.dp),
                onOpenDevTools = { onEvent(SettingsUiEvent.NavigateToDevTools) },
            )
        }

        AppBar(
            modifier = Modifier.padding(top = 16.dp),
            title = "Settings",
            leadingItems = listOf(
                AppBarItem(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onEvent(SettingsUiEvent.NavigateBack) },
                ),
            ),
        )
    }
}

@Composable
private fun AppearanceSection(
    modifier: Modifier = Modifier,
    uiMode: UIMode,
    dynamicTheme: Boolean,
    onUiModeUpdated: (UIMode) -> Unit,
    onDynamicThemeToggled: (Boolean) -> Unit,
) = Section(modifier = modifier, title = "Appearance") {
    Item(
        leadingContent = { Icon(Icons.Default.Brightness6, null) },
        headlineContent = { Text("UI Mode") },
        trailingContent = {
            Dropdown(
                selectedItem = DropdownItem(uiMode.displayName, uiMode, leadingIcon = uiMode.icon),
                items = UIMode.entries.map {
                    DropdownItem(name = it.displayName, value = it, leadingIcon = it.icon)
                },
                onItemSelected = { onUiModeUpdated(it.value) },
            )
        }
    )

    Item(
        leadingContent = { Icon(Icons.Default.Palette, null) },
        headlineContent = { Text("Dynamic Theme") },
        trailingContent = {
            Switch(
                modifier = Modifier.height(32.dp),
                checked = dynamicTheme,
                onCheckedChange = onDynamicThemeToggled,
            )
        },
        showDivider = false,
    )
}

@Composable
private fun BehaviorSection(
    modifier: Modifier = Modifier,
) = Section(modifier = modifier, title = "Behavior") {
    Item(
        leadingContent = { Icon(Icons.Default.CalendarToday, null) },
        headlineContent = { Text("Start week from Monday") },
        trailingContent = { Text("TODO") }
    )

    Item(
        leadingContent = { Icon(Icons.Default.CenterFocusStrong, null) },
        headlineContent = { Text("Highlight current day") },
        trailingContent = {
            Switch(
                modifier = Modifier.height(32.dp),
                checked = true,
                onCheckedChange = { }
            )
        },
        showDivider = false,
    )
}

@Composable
private fun AboutSection(
    modifier: Modifier = Modifier,
) = Section(modifier = modifier, title = "About") {

    val uriHandler = LocalUriHandler.current
    Item(
        leadingContent = {
            Image(
                modifier = Modifier.scale(1.8f).size(24.dp),
                painter = painterResource(R.drawable.limitless_icon_foreground),
                contentDescription = null,
            )
        },
        headlineContent = { Text("Limitless v${BuildConfig.VERSION_NAME}") },
    )

    Item(
        leadingContent = { Icon(Icons.Default.Person, null) },
        headlineContent = { Text("Developed by Ronak Harkhani") },
    )

    Item(
        onClick = { uriHandler.openUri(Link.AXON_DRAGON_SCALE) },
        leadingContent = {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_github),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
        },
        headlineContent = { Text("Github") },
        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
    )

    Item(
        onClick = { uriHandler.openUri(Link.LIMITLESS) },
        leadingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) },
        headlineContent = { Text("Source Code") },
        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
        showDivider = false
    )
}

@Composable
private fun DevToolsSection(
    modifier: Modifier = Modifier,
    onOpenDevTools: () -> Unit,
) = Section(modifier = modifier, title = "Dev Tools") {
    Item(
        leadingContent = { Icon(Icons.Default.Android, null) },
        headlineContent = { Text("Open Dev Tools") },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) },
        onClick = onOpenDevTools,
        showDivider = false,
    )
}

@Composable
private fun Section(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) = Column(
    modifier = modifier.padding(horizontal = 12.dp),
) {
    Text(
        modifier = Modifier.padding(start = 8.dp),
        text = title,
        fontWeight = FontWeight.Bold,
    )

    content()
}

@Composable
private fun Item(
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
) {
    ListItem(
        modifier = modifier.thenIf(onClick != null) { clickable(onClick = onClick!!) },
        leadingContent = leadingContent,
        headlineContent = {
            val style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            CompositionLocalProvider(LocalTextStyle provides style) {
                headlineContent()
            }
        },
        supportingContent = {
            val style = MaterialTheme.typography.labelSmall
            CompositionLocalProvider(LocalTextStyle provides style) {
                supportingContent?.invoke()
            }
        },
        trailingContent = trailingContent,
    )

    if (showDivider)
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LimitlessTheme {
        val uiState by remember { mutableStateOf(SettingsUiState()) }
        Settings(
            uiState = uiState,
            onEvent = { },
        )
    }
}
