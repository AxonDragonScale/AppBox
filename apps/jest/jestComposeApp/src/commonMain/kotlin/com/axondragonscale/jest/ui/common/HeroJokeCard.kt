package com.axondragonscale.jest.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder

import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.axondragonscale.jest.ui.component.shimmer
import com.axondragonscale.jest.ui.theme.DarkError
import com.axondragonscale.jest.ui.theme.JestTheme
import com.axondragonscale.jest.ui.theme.categoryColor
import com.axondragonscale.jest.ui.theme.categoryContainerColor
import com.axondragonscale.jest.util.shareText
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Hero joke card for the Home screen, matching the "Hero Joke Card" component in jest.pen.
 *
 * Layout (dark surface #16161A, 20dp padding, 12dp gap, 20dp corner radius):
 *   1. Top row: Category badge (colored pill) + Favorite toggle button
 *   2. Setup text  — Fraunces, white, 18sp, weight 600
 *   3. Punchline text — Fraunces, primary indigo, 18sp, weight 600  (TwoPart only)
 *   4. Bottom row: Share button (outlined) + "Next Joke" CTA (indigo filled)
 */
@Composable
fun HeroJokeCard(
    modifier: Modifier = Modifier,
    joke: IJoke?,
    shouldAnimateJoke: Boolean = true,
    onAnimationComplete: () -> Unit = {},
    onFavoriteToggled: (Boolean) -> Unit,
    onNextJoke: () -> Unit,
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
            if (joke != null) {
                CategoryBadge(categoryName = joke.category.name)
            } else {
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(80.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer(true, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                )
            }

            HeroFavoriteButton(
                isFavorite = joke?.favorite ?: false,
                isEnabled = joke != null,
                onFavoriteToggled = onFavoriteToggled,
            )
        }

        // Setup / joke text — Fraunces, white
        if (joke != null) {
            AnimatedContent(targetState = joke.getFirstLine()) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        lineHeight = 25.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            ShimmerText()
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerText(widthFraction = 0.7f)
        }

        // Punchline text — Fraunces, primary indigo (TwoPart jokes only)
        val punchline = joke?.getSecondLine()
        if (joke != null && punchline != null) {
            AnimatedContent(targetState = punchline) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp,
                        lineHeight = 25.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Bottom row — share button + next joke CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroShareButton(joke = joke)

            Button(
                onClick = onNextJoke,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp,
                    vertical = 0.dp,
                ),
                modifier = Modifier.height(36.dp),
            ) {
                Text(
                    text = "Next Joke",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    modifier: Modifier = Modifier,
    categoryName: String,
) {
    val isDark = true // TODO: read from LocalDarkTheme when available
    val textColor = categoryColor(categoryName, isDark)
    val bgColor = categoryContainerColor(categoryName, isDark)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = categoryName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun HeroFavoriteButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    isEnabled: Boolean,
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
            .then(if (isEnabled) Modifier.clickable { onFavoriteToggled(!isFavorite) } else Modifier),
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
private fun HeroShareButton(
    modifier: Modifier = Modifier,
    joke: IJoke?,
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
            )
            .clip(RoundedCornerShape(10.dp))
            .then(if (joke != null) Modifier.clickable { shareText(joke.getShareableText()) } else Modifier)
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

@Composable
private fun ShimmerText(
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.9f,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .shimmer(true, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
    )
}

@Preview
@Composable
private fun HeroJokeCardOnePartPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            HeroJokeCard(
                joke = OnePartJoke(
                    id = 1,
                    lang = Language.English,
                    category = Category.Pun,
                    flags = Flags(false, false, false, false, false, false),
                    safe = true,
                    favorite = false,
                    timestamp = 1L,
                    type = JokeType.Single,
                    joke = "Why don't scientists trust atoms? Because they make up everything."
                ),
                onFavoriteToggled = {},
                onNextJoke = {},
            )
        }
    }
}

@Preview
@Composable
private fun HeroJokeCardTwoPartPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            HeroJokeCard(
                joke = TwoPartJoke(
                    id = 2,
                    lang = Language.English,
                    category = Category.Programming,
                    flags = Flags(false, false, false, false, false, false),
                    safe = true,
                    favorite = true,
                    timestamp = 1L,
                    type = JokeType.TwoPart,
                    setup = "What happened to the man who got behind on payments to his exorcist?",
                    delivery = "He got repossessed.",
                ),
                onFavoriteToggled = {},
                onNextJoke = {},
            )
        }
    }
}

@Preview
@Composable
private fun HeroJokeCardShimmerPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            HeroJokeCard(
                joke = null,
                onFavoriteToggled = {},
                onNextJoke = {},
            )
        }
    }
}
