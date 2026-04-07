package com.axondragonscale.jest.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.axondragonscale.jest.ui.util.WindowSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.Flags
import com.axondragonscale.jest.model.IJoke
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.TwoPartJoke
import com.axondragonscale.jest.ui.common.LibraryJokeCard
import com.axondragonscale.jest.ui.component.FilterChip
import com.axondragonscale.jest.ui.filter.FilterHost
import com.axondragonscale.jest.ui.filter.FilterType
import com.axondragonscale.jest.ui.filter.PrefsVM
import com.axondragonscale.jest.ui.theme.DarkBorder
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by Ronak Harkhani on 24/10/24
 */

@Composable
fun Library(
    modifier: Modifier = Modifier,
    windowSize: WindowSize = WindowSize.Compact,
    vm: LibraryVM = koinViewModel(),
    prefsVm: PrefsVM = koinViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val prefsUiState by prefsVm.uiState.collectAsStateWithLifecycle()
    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var categoryChipAnchor by remember { mutableStateOf(IntOffset.Zero) }

    Library(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        categorySelectedCount = prefsUiState.selectedCategories.size,
        onEvent = { event ->
            when (event) {
                else -> vm.onEvent(event)
            }
        },
        onFilterClick = { isFilterSheetOpen = true },
        onCategoryChipPositioned = { categoryChipAnchor = it },
    )

    if (isFilterSheetOpen) {
        FilterHost(
            filterType = FilterType.Category,
            windowSize = windowSize,
            anchorOffset = categoryChipAnchor,
            onDismiss = { isFilterSheetOpen = false },
        )
    }
}

@Composable
private fun Library(
    modifier: Modifier = Modifier,
    uiState: LibraryUiState,
    windowSize: WindowSize,
    categorySelectedCount: Int = 0,
    onEvent: (LibraryUiEvent) -> Unit,
    onFilterClick: () -> Unit,
    onCategoryChipPositioned: (IntOffset) -> Unit = {},
) {
    val isCompact = windowSize == WindowSize.Compact
    val useGrid = !isCompact  // 2-column grid on tablet/desktop
    val horizontalPadding = when (windowSize) {
        WindowSize.Compact -> 20.dp
        WindowSize.Medium -> 24.dp
        WindowSize.Expanded -> 32.dp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (isCompact) Modifier.statusBarsPadding() else Modifier),
    ) {
        // Header + filter row
        LibraryHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = 20.dp, bottom = 16.dp),
            activeFilter = uiState.activeFilter,
            categorySelectedCount = categorySelectedCount,
            onFilterChanged = { onEvent(LibraryUiEvent.FilterChanged(it)) },
            onFilterChipClick = onFilterClick,
            onCategoryChipPositioned = onCategoryChipPositioned,
        )

        if (uiState.jokes.isEmpty() && !uiState.isLoading) {
            NoJokesMessage(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = 32.dp),
                activeFilter = uiState.activeFilter,
            )
        } else if (useGrid) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = 8.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.jokes, key = { it.id }) { joke ->
                    LibraryJokeCard(
                        joke = joke,
                        onFavoriteToggled = { favorite ->
                            onEvent(LibraryUiEvent.FavoriteToggled(joke.id, favorite))
                        },
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding,
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.jokes, key = { it.id }) { joke ->
                    LibraryJokeCard(
                        joke = joke,
                        onFavoriteToggled = { favorite ->
                            onEvent(LibraryUiEvent.FavoriteToggled(joke.id, favorite))
                        },
                    )
                }

                // Bottom padding for floating bottom bar
                item { Box(modifier = Modifier.padding(bottom = 80.dp)) }
            }
        }
    }
}

@Composable
private fun LibraryHeader(
    modifier: Modifier = Modifier,
    activeFilter: LibraryFilter,
    categorySelectedCount: Int = 0,
    onFilterChanged: (LibraryFilter) -> Unit,
    onFilterChipClick: () -> Unit,
    onCategoryChipPositioned: (IntOffset) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Screen title
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Filter row: All / Favorites toggle + Category/Flags chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // All / Favorites filter toggle chips
            FilterToggleChip(
                label = "All",
                isActive = activeFilter == LibraryFilter.All,
                onClick = { onFilterChanged(LibraryFilter.All) },
            )
            FilterToggleChip(
                label = "Favorites",
                isActive = activeFilter == LibraryFilter.Favorites,
                onClick = { onFilterChanged(LibraryFilter.Favorites) },
            )

            // Spacer to push joke filter chips to the right
            Box(modifier = Modifier.weight(1f))

            FilterChip(
                modifier = Modifier.onGloballyPositioned { coords ->
                    val pos = coords.positionInWindow()
                    onCategoryChipPositioned(
                        IntOffset(pos.x.toInt(), (pos.y + coords.size.height).toInt())
                    )
                },
                label = "Category",
                icon = Icons.Default.LocalOffer,
                selectedCount = categorySelectedCount,
                onClick = onFilterChipClick,
            )
        }
    }
}

@Composable
private fun FilterToggleChip(
    modifier: Modifier = Modifier,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
        label = label,
        icon = Icons.Default.LocalOffer,
        isActive = isActive,
        onClick = onClick,
    )
}

@Composable
private fun NoJokesMessage(
    modifier: Modifier = Modifier,
    activeFilter: LibraryFilter,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = Icons.Outlined.EmojiEmotions,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )
        }

        Text(
            text = if (activeFilter == LibraryFilter.Favorites) "No Favorites Yet!" else "No Jokes Yet!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = if (activeFilter == LibraryFilter.Favorites)
                "Tap the heart on any joke to save it here"
            else
                "Start exploring and your joke history will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun LibraryPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Library(
                uiState = LibraryUiState(
                    jokes = listOf(
                        OnePartJoke(
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
                        TwoPartJoke(
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
                    ),
                    activeFilter = LibraryFilter.All,
                    isLoading = false,
                ),
                windowSize = WindowSize.Compact,
                onEvent = {},
                onFilterClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LibraryEmptyPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Library(
                uiState = LibraryUiState(jokes = emptyList(), isLoading = false),
                windowSize = WindowSize.Compact,
                onEvent = {},
                onFilterClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LibraryFavoritesEmptyPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Library(
                uiState = LibraryUiState(
                    jokes = emptyList(),
                    activeFilter = LibraryFilter.Favorites,
                    isLoading = false,
                ),
                windowSize = WindowSize.Compact,
                onEvent = {},
                onFilterClick = {},
            )
        }
    }
}
