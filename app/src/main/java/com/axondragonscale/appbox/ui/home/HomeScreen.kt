package com.axondragonscale.appbox.ui.home

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import com.axondragonscale.appbox.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.appbox.ui.theme.AppBoxTheme
import com.axondragonscale.jest.JestActivity
import com.axondragonscale.jest.R as JestR
import com.axondragonscale.wallmatic.R as WallmaticR
import com.axondragonscale.wallmatic.WallmaticActivity

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = statusBarPadding.calculateTopPadding() + 16.dp,
            bottom = 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header section - spans full width
        item(span = { GridItemSpan(2) }) {
            HeaderSection()
        }

        // Sub-apps grid
        items(subApps) { app ->
            SubAppCard(app = app) {
                app.onClick(context)
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column {
                Text(
                    text = "AppBox",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your apps, all in one place",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

    }
}

@Composable
private fun SubAppCard(app: SubApp, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (app.enabled) 1f else 0.98f,
        label = "scale"
    )

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val disabledSurfaceColor = MaterialTheme.colorScheme.surfaceContainerLow

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .scale(scale),
        onClick = onClick,
        enabled = app.enabled,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = if (app.enabled) {
                            listOf(
                                surfaceColor,
                                app.accentColor.copy(alpha = 0.25f)
                            )
                        } else {
                            listOf(
                                disabledSurfaceColor,
                                disabledSurfaceColor
                            )
                        },
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (app.enabled) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (val icon = app.icon) {
                        is SubAppIcon.Vector -> {
                            Icon(
                                imageVector = icon.imageVector,
                                contentDescription = null,
                                tint = if (app.enabled) {
                                    app.accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        is SubAppIcon.Drawable -> {
                            Image(
                                painter = painterResource(id = icon.resId),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                colorFilter = if (!app.enabled) {
                                    ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                } else null
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (app.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (app.enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        maxLines = 2
                    )

                    if (!app.enabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Coming Soon",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface SubAppIcon {
    data class Vector(val imageVector: ImageVector) : SubAppIcon
    data class Drawable(@DrawableRes val resId: Int) : SubAppIcon
}

private data class SubApp(
    val name: String,
    val description: String,
    val icon: SubAppIcon,
    val accentColor: Color,
    val enabled: Boolean = true,
    val onClick: (Context) -> Unit = {}
)

private val subApps = listOf(
    SubApp(
        name = "Jest",
        description = "Get your daily dose of humor",
        icon = SubAppIcon.Drawable(JestR.drawable.jest_icon_foreground),
        accentColor = Color(0xFFFFD248),
        onClick = { JestActivity.start(it) }
    ),
    SubApp(
        name = "Wallmatic",
        description = "Auto-change wallpapers on schedule",
        icon = SubAppIcon.Drawable(WallmaticR.drawable.wallmatic_icon_foreground),
        accentColor = Color(0xFF4B46C0),
        onClick = { WallmaticActivity.start(it) }
    ),
    SubApp(
        name = "Topty",
        description = "Lightweight 2FA authenticator",
        icon = SubAppIcon.Vector(Icons.Rounded.Key),
        accentColor = Color(0xFF2196F3),
        enabled = false,
    ),
    SubApp(
        name = "2048",
        description = "Classic puzzle game",
        icon = SubAppIcon.Vector(Icons.Rounded.GridView),
        accentColor = Color(0xFFE91E63),
        enabled = false,
    )
)

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppBoxTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HomeScreen()
        }
    }
}
