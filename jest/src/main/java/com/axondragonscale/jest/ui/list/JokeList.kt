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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PostAdd
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
import com.axondragonscale.appdump.jest.model.Category
import com.axondragonscale.appdump.jest.model.Flags
import com.axondragonscale.appdump.jest.model.JokeType
import com.axondragonscale.appdump.jest.model.Language
import com.axondragonscale.appdump.jest.model.OnePartJoke
import com.axondragonscale.appdump.jest.model.TwoPartJoke
import com.axondragonscale.appdump.jest.ui.common.JokeCard
import com.axondragonscale.appdump.jest.ui.theme.JestTheme
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
            Text(
                modifier = Modifier.padding(vertical = 32.dp),
                text = listType.name,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
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
    Column(
        modifier = modifier.fillMaxWidth().height(600.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(100.dp),
            imageVector = Icons.Default.PostAdd,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )

        Text(
            modifier = Modifier.padding(top = 48.dp),
            text = "Whoops!!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Looks like you don't have jokes here.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
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
