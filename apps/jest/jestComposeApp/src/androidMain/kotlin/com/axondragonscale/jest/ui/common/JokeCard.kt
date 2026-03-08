package com.axondragonscale.jest.ui.common

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.Flags
import com.axondragonscale.jest.model.IJoke
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.getFirstLine
import com.axondragonscale.jest.model.getSecondLine
import com.axondragonscale.jest.model.getShareIntent
import com.axondragonscale.jest.ui.component.JestTag
import com.axondragonscale.jest.ui.component.TypewriterText
import com.axondragonscale.jest.ui.component.TypewriterTextVisibility
import com.axondragonscale.jest.ui.component.shimmer
import com.axondragonscale.jest.ui.theme.JestTheme
import kotlinx.coroutines.delay

/**
 * Created by Ronak Harkhani on 09/04/24
 */

@Composable
fun JokeCard(
    modifier: Modifier = Modifier,
    joke: IJoke?,
    shouldAnimateJoke: Boolean,
    onAnimationComplete: () -> Unit,
    onFavoriteToggled: (Boolean) -> Unit,
) = Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    shape = RoundedCornerShape(16.dp),
) {
    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
    ) {
        OpeningQuote()

        if (joke != null) {
            JokeText(
                firstLine = joke.getFirstLine(),
                secondLine = joke.getSecondLine(),
                isTextAnimated = shouldAnimateJoke,
                onAnimationComplete = onAnimationComplete,
            )
        } else {
            Shimmer(modifier = Modifier.fillMaxWidth(0.9f))
            Spacer(modifier = Modifier.height(8.dp))
            Shimmer(modifier = Modifier.fillMaxWidth(0.9f))
        }

        ClosingQuote(modifier = Modifier.align(Alignment.End))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FavoriteButton(
                isFavorite = joke?.favorite ?: false,
                onFavoriteToggled = onFavoriteToggled,
                isEnabled = joke != null,
            )
            ShareButton(joke = joke)
            Spacer(modifier = Modifier.weight(1f))

            if (joke != null) JestTag(tag = joke.category.name)
            else Shimmer(modifier = Modifier.width(48.dp))
        }
    }
}


@Composable
private fun JokeText(
    firstLine: String,
    secondLine: String?,
    isTextAnimated: Boolean,
    onAnimationComplete: () -> Unit,
) {
    var secondLineVisibility by remember(secondLine) {
        mutableStateOf(
            if (isTextAnimated) TypewriterTextVisibility.Invisible
            else TypewriterTextVisibility.Static
        )
    }
    TypewriterText(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 28.dp),
        text = firstLine,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        visibility = if (isTextAnimated) TypewriterTextVisibility.Animate else TypewriterTextVisibility.Static,
        onEffectComplete = {
            if (secondLine != null) {
                delay(1000)
                secondLineVisibility = TypewriterTextVisibility.Animate
            } else {
                onAnimationComplete.invoke()
            }
        }
    )

    secondLine?.let {
        TypewriterText(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 28.dp),
            text = it,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            visibility = secondLineVisibility,
            onEffectComplete = onAnimationComplete
        )
    }
}

@Composable
private fun Shimmer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(35))
            .shimmer(true, MaterialTheme.colorScheme.onPrimary)
    )
}

@Composable
private fun FavoriteButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onFavoriteToggled: (Boolean) -> Unit,
    isEnabled: Boolean,
) {
    IconToggleButton(
        modifier = modifier,
        checked = isFavorite,
        onCheckedChange = onFavoriteToggled,
        enabled = isEnabled,
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContentColor = LocalContentColor.current
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
        )
    }
}

@Composable
private fun ShareButton(
    modifier: Modifier = Modifier,
    joke: IJoke?,
) {
    val context = LocalContext.current
    IconButton(
        modifier = modifier,
        enabled = joke != null,
        onClick = {
            if (joke != null)
                context.startActivity(joke.getShareIntent())
        },
    ) {
        Icon(
            imageVector = Icons.Default.IosShare,
            contentDescription = null
        )
    }
}

@Composable
private fun OpeningQuote(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .offset(x = -4.dp)
            .padding(bottom = 8.dp)
            .size(32.dp)
            .graphicsLayer(rotationY = 180f),
        imageVector = Icons.Default.FormatQuote,
        contentDescription = null
    )
}

@Composable
private fun ClosingQuote(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .padding(top = 4.dp)
            .size(20.dp)
            .offset(x = 4.dp),
        imageVector = Icons.Default.FormatQuote,
        contentDescription = null
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JokeCardPreview() {
    JestTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            JokeCard(
                joke = OnePartJoke(
                    id = 1,
                    lang = Language.English,
                    category = Category.Pun,
                    flags = Flags(false, false, false, false, false, false),
                    safe = true,
                    favorite = false,
                    timestamp = 1,
                    type = JokeType.Single,
                    joke = "Lorem Ipsum"
                ),
                shouldAnimateJoke = true,
                onAnimationComplete = {},
                onFavoriteToggled = {},
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JokeCardShimmerPreview() {
    JestTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            JokeCard(
                joke = null,
                shouldAnimateJoke = false,
                onAnimationComplete = {},
                onFavoriteToggled = {},
            )
        }
    }
}
