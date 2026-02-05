package com.axondragonscale.jest.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.axondragonscale.jest.model.TwoPartJoke
import com.axondragonscale.jest.ui.common.JokeCard
import com.axondragonscale.jest.ui.theme.JestTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 07/04/24
 */

@Composable
fun Favorites(vm: FavoritesVM = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    JokeList(
        listType = JokeListType.Favorites,
        uiState = uiState,
        onEvent = { vm.onEvent(it) },
    )
}

@Composable
fun History(vm: HistoryVM = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    JokeList(
        listType = JokeListType.History,
        uiState = uiState,
        onEvent = { vm.onEvent(it) },
    )
}

@Composable
fun JokeList(
    modifier: Modifier = Modifier,
    listType: JokeListType,
    uiState: JokeListUiState,
    onEvent: (JokeListUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = listType.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        item {
            if (uiState.jokes.isEmpty())
                NoJokesMessage()
        }

        items(uiState.jokes, key = { it.id }) { joke ->
            JokeCard(
                joke = joke,
                shouldAnimateJoke = false,
                onAnimationComplete = { },
                onFavoriteToggled = {
                    onEvent(JokeListUiEvent.FavoriteToggled(joke.id, it))
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun NoJokesMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(56.dp),
                    imageVector = Icons.Outlined.EmojiEmotions,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Jokes Yet!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start exploring and add some jokes\nto see them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JokeListPreview() {
    JestTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            JokeList(
                listType = JokeListType.History,
                uiState = JokeListUiState(
                    listOf(
                        OnePartJoke(
                            id = 1,
                            lang = Language.English,
                            category = Category.Pun,
                            flags = Flags(false, false, false, false, false, false),
                            safe = true,
                            timestamp = 1,
                            type = JokeType.Single,
                            joke = "Lorem Ipsum"
                        ),
                        TwoPartJoke(
                            id = 2,
                            lang = Language.English,
                            category = Category.Pun,
                            flags = Flags(false, false, false, false, false, false),
                            safe = true,
                            timestamp = 1,
                            type = JokeType.TwoPart,
                            setup = "Lorem Ipsum",
                            delivery = "Lorem Ipsum"
                        )
                    )
                ),
                onEvent = { }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyJokeListPreview() {
    JestTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            JokeList(
                listType = JokeListType.Favorites,
                uiState = JokeListUiState(listOf()),
                onEvent = { }
            )
        }
    }
}
