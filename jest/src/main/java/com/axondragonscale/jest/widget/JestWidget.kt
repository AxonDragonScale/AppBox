package com.axondragonscale.jest.widget

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.axondragonscale.jest.JestActivity
import com.axondragonscale.jest.R
import com.axondragonscale.jest.model.IJoke
import com.axondragonscale.jest.model.getFirstLine
import com.axondragonscale.jest.model.getSecondLine

/**
 * Created by Ronak Harkhani on 20/04/24
 */

@Composable
fun JestWidget(joke: IJoke) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.primary)
            .padding(12.dp)
            .clickable(actionStartActivity(JestActivity::class.java)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = GlanceModifier.defaultWeight())
        OpeningQuote()
        WidgetText(
            modifier = GlanceModifier.padding(horizontal = 16.dp),
            text = joke.getFirstLine()
        )
        joke.getSecondLine()?.let { secondLine ->
            WidgetText(
                modifier = GlanceModifier.padding(horizontal = 16.dp).padding(top = 8.dp),
                text = secondLine
            )
        }
        ClosingQuote()
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FavoriteButton(joke = joke)
            ShuffleButton()
            Spacer(modifier = GlanceModifier.defaultWeight())
            WidgetTag(text = joke.category.name)
        }
    }
}

@Composable
private fun WidgetText(
    modifier: GlanceModifier = GlanceModifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            color = GlanceTheme.colors.onPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    )
}


@Composable
private fun OpeningQuote(modifier: GlanceModifier = GlanceModifier) {
    WidgetIcon(
        modifier = modifier.size(32.dp),
        resId = R.drawable.ic_opening_quote,
    )
}

@Composable
private fun ClosingQuote(modifier: GlanceModifier = GlanceModifier) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        WidgetIcon(
            modifier = modifier.size(20.dp),
            resId = R.drawable.ic_closing_quote,
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
    }
}

@Composable
private fun FavoriteButton(
    modifier: GlanceModifier = GlanceModifier,
    joke: IJoke,
) {
    WidgetIcon(
        modifier = modifier
            .cornerRadius(24.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(actionRunCallback<FavoriteAction>(
                actionParametersOf(
                    FavoriteAction.jokeIdKey to joke.id,
                    FavoriteAction.favoriteKey to !joke.favorite
                )
            )),
        resId = if (joke.favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
    )
}

@Composable
private fun ShuffleButton(modifier: GlanceModifier = GlanceModifier) {
    WidgetIcon(
        modifier = modifier
            .cornerRadius(24.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(actionRunCallback<ShuffleAction>()),
        resId = R.drawable.ic_shuffle,
    )
}

@Composable
private fun WidgetTag(
    modifier: GlanceModifier = GlanceModifier,
    text: String,
) {
    Text(
        modifier = modifier
            .background(GlanceTheme.colors.onPrimary)
            .cornerRadius(8.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        text = text.uppercase(),
        style = TextStyle(
            color = GlanceTheme.colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    )
}

@Composable
private fun WidgetIcon(
    modifier: GlanceModifier = GlanceModifier,
    @DrawableRes resId: Int,
    tintColor: ColorProvider = GlanceTheme.colors.onPrimary,
) {
    Image(
        modifier = modifier,
        provider = ImageProvider(resId = resId),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tintColor)
    )
}
