package com.axondragonscale.wallmatic.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.axondragonscale.wallmatic.ui.albums.Albums
import com.axondragonscale.wallmatic.ui.bottombar.Tab
import com.axondragonscale.wallmatic.ui.component.Masthead
import com.axondragonscale.wallmatic.ui.component.WallmaticTabBar
import com.axondragonscale.wallmatic.ui.home.Home
import com.axondragonscale.wallmatic.ui.settings.Settings
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 23/06/24
 */

@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    navController: NavController,
    tab: Tab,
) {
    val pagerState = rememberPagerState(
        initialPage = tab.ordinal,
        pageCount = { Tab.entries.size },
    )
    val scope = rememberCoroutineScope()
    val activeTab = Tab.entries[pagerState.currentPage]
    var createAlbumRequest by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.wallmatic.paper)
            .statusBarsPadding(),
    ) {
        Masthead(section = activeTab.label)

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { position ->
            when (Tab.entries[position]) {
                Tab.Home -> Home(
                    navController = navController,
                    onNavigateToLibrary = {
                        createAlbumRequest = true
                        scope.launch { pagerState.animateScrollToPage(Tab.Albums.ordinal) }
                    },
                )
                Tab.Albums -> Albums(
                    navController = navController,
                    openCreateAlbumDialog = createAlbumRequest,
                    onOpenCreateAlbumDialogHandled = { createAlbumRequest = false },
                )
                Tab.Settings -> Settings(navController = navController)
            }
        }

        WallmaticTabBar(
            active = activeTab,
            onSelect = { selected ->
                scope.launch { pagerState.animateScrollToPage(selected.ordinal) }
            },
        )
    }
}
