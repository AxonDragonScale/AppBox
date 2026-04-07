package com.axondragonscale.jest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
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
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.TwoPartJoke
import com.axondragonscale.jest.ui.common.HeroJokeCard
import com.axondragonscale.jest.ui.component.FilterChip
import com.axondragonscale.jest.ui.component.StatCard
import com.axondragonscale.jest.ui.filter.FilterHost
import com.axondragonscale.jest.ui.filter.FilterType
import com.axondragonscale.jest.ui.filter.PrefsVM
import com.axondragonscale.jest.ui.theme.DarkError
import com.axondragonscale.jest.ui.theme.DarkSecondary
import com.axondragonscale.jest.ui.theme.DarkSuccess
import com.axondragonscale.jest.ui.theme.JestTheme

import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by Ronak Harkhani on 01/04/24
 */

@Composable
fun Home(
    modifier: Modifier = Modifier,
    windowSize: WindowSize = WindowSize.Compact,
    vm: HomeVM = koinViewModel(),
    prefsVm: PrefsVM = koinViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val prefsUiState by prefsVm.uiState.collectAsStateWithLifecycle()
    var activeFilterType by remember { mutableStateOf<FilterType?>(null) }
    var categoryChipAnchor by remember { mutableStateOf(IntOffset.Zero) }
    var flagsChipAnchor by remember { mutableStateOf(IntOffset.Zero) }

    Home(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        categorySelectedCount = prefsUiState.selectedCategories.size,
        flagsSelectedCount = prefsUiState.selectedBlacklistFlags.size,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.OpenCategoryFilter -> activeFilterType = FilterType.Category
                is HomeUiEvent.OpenFlagsFilter -> activeFilterType = FilterType.Flags
                is HomeUiEvent.CloseFilter -> activeFilterType = null
                else -> vm.onEvent(event)
            }
        },
        onCategoryChipPositioned = { categoryChipAnchor = it },
        onFlagsChipPositioned = { flagsChipAnchor = it },
    )

    activeFilterType?.let { filterType ->
        FilterHost(
            filterType = filterType,
            windowSize = windowSize,
            anchorOffset = when (filterType) {
                FilterType.Category -> categoryChipAnchor
                FilterType.Flags -> flagsChipAnchor
            },
            onDismiss = { activeFilterType = null },
        )
    }
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    windowSize: WindowSize,
    categorySelectedCount: Int = 0,
    flagsSelectedCount: Int = 0,
    onEvent: (HomeUiEvent) -> Unit,
    onCategoryChipPositioned: (IntOffset) -> Unit = {},
    onFlagsChipPositioned: (IntOffset) -> Unit = {},
) {
    val isCompact = windowSize == WindowSize.Compact
    val horizontalPadding = when (windowSize) {
        WindowSize.Compact -> 20.dp
        WindowSize.Medium -> 24.dp
        WindowSize.Expanded -> 32.dp
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .then(if (isCompact) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = horizontalPadding)
            .padding(top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header — only shown in compact (mobile) mode; tablet/desktop use the top bar
        if (isCompact) {
            HomeHeader()
        }

        // Filter chips row
        FilterRow(
            categorySelectedCount = categorySelectedCount,
            flagsSelectedCount = flagsSelectedCount,
            onEvent = onEvent,
            onCategoryChipPositioned = onCategoryChipPositioned,
            onFlagsChipPositioned = onFlagsChipPositioned,
        )

        // Hero joke card
        HeroJokeCard(
            joke = uiState.joke,
            shouldAnimateJoke = uiState.shouldAnimateJoke,
            onAnimationComplete = { onEvent(HomeUiEvent.JokeAnimationComplete) },
            onFavoriteToggled = { favorite ->
                uiState.joke?.let { onEvent(HomeUiEvent.FavoriteToggled(it, favorite)) }
            },
            onNextJoke = { onEvent(HomeUiEvent.NewJoke) },
        )

        // Stats row
        StatsRow(
            jokeCount = uiState.jokeCount,
            favoriteCount = uiState.favoriteCount,
        )
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Jest",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Your daily dose of humour",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilterRow(
    modifier: Modifier = Modifier,
    categorySelectedCount: Int = 0,
    flagsSelectedCount: Int = 0,
    onEvent: (HomeUiEvent) -> Unit,
    onCategoryChipPositioned: (IntOffset) -> Unit = {},
    onFlagsChipPositioned: (IntOffset) -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            onClick = { onEvent(HomeUiEvent.OpenCategoryFilter) },
        )
        FilterChip(
            modifier = Modifier.onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                onFlagsChipPositioned(
                    IntOffset(pos.x.toInt(), (pos.y + coords.size.height).toInt())
                )
            },
            label = "Flags",
            icon = Icons.Default.Shield,
            selectedCount = flagsSelectedCount,
            onClick = { onEvent(HomeUiEvent.OpenFlagsFilter) },
        )
    }
}

@Composable
private fun StatsRow(
    modifier: Modifier = Modifier,
    jokeCount: Int,
    favoriteCount: Int,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = jokeCount.toString(),
            label = "Seen",
            valueColor = DarkSuccess,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = favoriteCount.toString(),
            label = "Liked",
            valueColor = DarkError,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "—",
            label = "Streak",
            valueColor = DarkSecondary,
        )
    }
}

@Preview
@Composable
private fun HomePreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Home(
                uiState = HomeUiState(
                    joke = TwoPartJoke(
                        id = 1,
                        lang = Language.English,
                        category = Category.Pun,
                        flags = Flags(false, false, false, false, false, false),
                        safe = true,
                        favorite = false,
                        timestamp = 1L,
                        type = JokeType.TwoPart,
                        setup = "What happened to the man who got behind on payments to his exorcist?",
                        delivery = "He got repossessed.",
                    ),
                    isLoading = false,
                    jokeCount = 142,
                    favoriteCount = 38,
                ),
                windowSize = WindowSize.Compact,
                onEvent = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeLoadingPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Home(
                uiState = HomeUiState(isLoading = true),
                windowSize = WindowSize.Compact,
                onEvent = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeOnePartPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Home(
                uiState = HomeUiState(
                    joke = OnePartJoke(
                        id = 2,
                        lang = Language.English,
                        category = Category.Programming,
                        flags = Flags(false, false, false, false, false, false),
                        safe = true,
                        favorite = true,
                        timestamp = 1L,
                        type = JokeType.Single,
                        joke = "Why don't scientists trust atoms? Because they make up everything.",
                    ),
                    isLoading = false,
                    jokeCount = 5,
                    favoriteCount = 2,
                ),
                windowSize = WindowSize.Compact,
                onEvent = {},
            )
        }
    }
}
