package com.axondragonscale.jest.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.bottombar.BottomTabBar
import com.axondragonscale.jest.ui.bottombar.Tab
import com.axondragonscale.jest.ui.home.Home
import com.axondragonscale.jest.ui.list.Favorites
import com.axondragonscale.jest.ui.list.History
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 01/04/24
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JestApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val pagerState = rememberPagerState { Tab.all.size }
        val scope = rememberCoroutineScope()

        HorizontalPager(state = pagerState) { position ->
            when (position) {
                Tab.Home.position -> Home()
                Tab.Favorites.position -> Favorites()
                Tab.History.position -> History()
            }
        }

        BottomTabBar(
            modifier = Modifier
                .padding(horizontal = 56.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            tabs = Tab.all,
            activeTab = Tab.all[pagerState.currentPage],
            onTabChange = {
                scope.launch {
                    pagerState.animateScrollToPage(it.position)
                }
            }
        )
    }
}
