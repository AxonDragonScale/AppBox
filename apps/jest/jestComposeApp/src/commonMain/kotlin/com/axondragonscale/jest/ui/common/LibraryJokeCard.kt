package com.axondragonscale.jest.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.Flags
import com.axondragonscale.jest.model.IJoke
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.TwoPartJoke
import com.axondragonscale.jest.model.getFirstLine
import com.axondragonscale.jest.model.getSecondLine
import com.axondragonscale.jest.model.getShareableText
import com.axondragonscale.jest.ui.theme.DarkError
import com.axondragonscale.jest.ui.theme.DarkTextSecondary
import com.axondragonscale.jest.ui.theme.JestTheme
import com.axondragonscale.jest.ui.theme.categoryColor
import com.axondragonscale.jest.ui.theme.categoryContainerColor
import com.axondragonscale.jest.util.shareText
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Library joke card matching the LibraryJokeCard component in jest.pen.
 *
 * Layout (dark surface #16161A, 20dp padding, 12dp gap, 20dp corner radius):
 *   1. Top row: Category badge (colored pill) + Favorite toggle button
 *   2. Joke text — Fraunces, white, 15sp, weight 600
 *   3. Bottom row: Share button + timestamp label
 */
@Composable
fun LibraryJokeCard(
    modifier: Modifier = Modifier,
    joke: IJoke,
    onFavoriteToggled: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top row — category badge + favorite button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LibraryCategoryBadge(categoryName = joke.category.name)
            LibraryFavoriteButton(
                isFavorite = joke.favorite,
                onFavoriteToggled = onFavoriteToggled,
            )
        }

        // Joke text — setup (and delivery for two-part)
        Text(
            text = joke.getFirstLine(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp,
                lineHeight = 21.sp,
            ),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        val punchline = joke.getSecondLine()
        if (punchline != null) {
            Text(
                text = punchline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 21.sp,
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Bottom row — share button + timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LibraryShareButton(joke = joke)
            Text(
                text = formatTimestamp(joke.timestamp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = DarkTextSecondary,
            )
        }
    }
}

@Composable
private fun LibraryCategoryBadge(
    modifier: Modifier = Modifier,
    categoryName: String,
) {
    val isDark = true
    val textColor = categoryColor(categoryName, isDark)
    val bgColor = categoryContainerColor(categoryName, isDark)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = categoryName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun LibraryFavoriteButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onFavoriteToggled: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .background(
                color = DarkError.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable { onFavoriteToggled(!isFavorite) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) DarkError else DarkError.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun LibraryShareButton(
    modifier: Modifier = Modifier,
    joke: IJoke,
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable { shareText(joke.getShareableText()) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            modifier = Modifier.size(15.dp),
            imageVector = Icons.Default.IosShare,
            contentDescription = "Share joke",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Share",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Formats a Unix epoch millisecond timestamp into a human-readable string.
 * e.g. "Today · 9:30 AM" or "Jan 15 · 2:00 PM"
 */
private fun formatTimestamp(epochMillis: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val tz = TimeZone.currentSystemDefault()
        val dt = instant.toLocalDateTime(tz)
        val hour = dt.hour
        val minute = dt.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val minuteStr = minute.toString().padStart(2, '0')
        val timeStr = "$displayHour:$minuteStr $amPm"
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthStr = monthNames.getOrElse(dt.monthNumber - 1) { "?" }
        "$monthStr ${dt.dayOfMonth} · $timeStr"
    } catch (e: Exception) {
        "—"
    }
}

@Preview
@Composable
private fun LibraryJokeCardOnePartPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LibraryJokeCard(
                joke = OnePartJoke(
                    id = 1,
                    lang = Language.English,
                    category = Category.Programming,
                    flags = Flags(false, false, false, false, false, false),
                    safe = true,
                    favorite = false,
                    timestamp = 1_700_000_000_000L,
                    type = JokeType.Single,
                    joke = "Why don't scientists trust atoms? Because they make up everything.",
                ),
                onFavoriteToggled = {},
            )
        }
    }
}

@Preview
@Composable
private fun LibraryJokeCardTwoPartPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LibraryJokeCard(
                joke = TwoPartJoke(
                    id = 2,
                    lang = Language.English,
                    category = Category.Pun,
                    flags = Flags(false, false, false, false, false, false),
                    safe = true,
                    favorite = true,
                    timestamp = 1_700_000_000_000L,
                    type = JokeType.TwoPart,
                    setup = "I told my wife she should embrace her mistakes.",
                    delivery = "She gave me a hug.",
                ),
                onFavoriteToggled = {},
            )
        }
    }
}
