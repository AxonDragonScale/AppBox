package com.axondragonscale.jest.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.Flags
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.ui.common.JokeCard
import com.axondragonscale.jest.ui.component.JestIconButton
import com.axondragonscale.jest.ui.theme.JestTheme
import com.axondragonscale.jest.ui.tune.PrefsBottomSheet
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 01/04/24
 */

@Composable
fun Home(vm: HomeVM = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    Home(
        uiState = uiState,
        onEvent = { vm.onEvent(it) }
    )
}

@Composable
fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        HomeHeader(modifier = Modifier.padding(top = 32.dp))

        Spacer(modifier = Modifier.weight(1f))

        (uiState as? HomeUiState.Success).let { uiState ->
            JokeCard(
                joke = uiState?.joke,
                shouldAnimateJoke = uiState?.shouldAnimateJoke ?: false,
                onAnimationComplete = { onEvent(HomeUiEvent.JokeAnimationComplete) },
                onFavoriteToggled = {
                    uiState?.joke?.let { joke ->
                        onEvent(HomeUiEvent.FavoriteToggled(joke, it))
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        var isTuneBottomSheetOpen by remember { mutableStateOf(false) }
        JokeControls(
            onTuneClick = { isTuneBottomSheetOpen = true },
            onRandomClick = { onEvent(HomeUiEvent.NewJoke) }
        )

        if (isTuneBottomSheetOpen)
            PrefsBottomSheet(onDismiss = { isTuneBottomSheetOpen = false })

        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun JokeControls(
    modifier: Modifier = Modifier,
    onTuneClick: () -> Unit,
    onRandomClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        JestIconButton(icon = Icons.Default.Tune, onClick = onTuneClick)
        Spacer(modifier = Modifier.width(16.dp))
        JestIconButton(icon = Icons.Default.Shuffle, onClick = onRandomClick)
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Jest",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Your daily dose of humour",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomePreview() {
    JestTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Home(
                modifier = Modifier,
                uiState = HomeUiState.Success(
                    joke = OnePartJoke(
                        id = 1,
                        lang = Language.English,
                        category = Category.Pun,
                        flags = Flags(false, false, false, false, false, false),
                        safe = true,
                        timestamp = 1,
                        type = JokeType.Single,
                        joke = "Lorem Ipsum"
                    )
                ),
                onEvent = {}
            )
        }
    }
}
