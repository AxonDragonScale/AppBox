package com.axondragonscale.topty.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.commons.compose.visible
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.onboarding.pages.AddTokenGuidePage
import com.axondragonscale.topty.ui.onboarding.pages.FeaturesPage
import com.axondragonscale.topty.ui.onboarding.pages.SetPasswordPage
import com.axondragonscale.topty.ui.onboarding.pages.WelcomePage
import com.axondragonscale.topty.ui.theme.ToptyTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 28/12/24
 */

@Composable
fun Onboarding(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: OnboardingVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            OnboardingUiEffect.NavigateToHome -> {
                navController.navigate(Route.Home) { popUpTo(Route.Onboarding) { inclusive = true } }
            }
            OnboardingUiEffect.NavigateToQRScanner -> {
                navController.navigate(Route.Home) { popUpTo(Route.Onboarding) { inclusive = true } }
                navController.navigate(Route.QRScanner)
            }
            OnboardingUiEffect.NavigateToAddToken -> {
                navController.navigate(Route.Home) { popUpTo(Route.Onboarding) { inclusive = true } }
                navController.navigate(Route.Token.Add(uri = null))
            }
            OnboardingUiEffect.NavigateToBackupRestore -> {
                navController.navigate(Route.Home) { popUpTo(Route.Onboarding) { inclusive = true } }
                navController.navigate(Route.Settings)
                navController.navigate(Route.BackupRestore)
            }
        }
    }

    Onboarding(
        modifier = modifier,
        uiState = uiState,
        onEvent = { viewModel.onEvent(it) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Onboarding(
    modifier: Modifier = Modifier,
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { uiState.totalPages }
    )

    LaunchedEffect(uiState.currentPage) {
        pagerState.animateScrollToPage(uiState.currentPage)
    }

    Scaffold(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        bottomBar = {
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                totalPages = uiState.totalPages,
                onBackClick = { onEvent(OnboardingUiEvent.PreviousPage) },
                onNextClick = { onEvent(OnboardingUiEvent.NextPage) },
                onSkipClick = { onEvent(OnboardingUiEvent.SkipOnboarding) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> SetPasswordPage(
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        passwordError = uiState.passwordError,
                        passwordStrength = uiState.passwordStrength,
                        onEvent = onEvent
                    )
                    2 -> FeaturesPage()
                    3 -> AddTokenGuidePage(
                        onScanQRCode = { onEvent(OnboardingUiEvent.NavigateToQRScanner) },
                        onManualEntry = { onEvent(OnboardingUiEvent.NavigateToManualEntry) },
                        onImport = { onEvent(OnboardingUiEvent.NavigateToImport) }
                    )
                }
            }

            PageIndicators(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp),
                currentPage = pagerState.currentPage,
                totalPages = uiState.totalPages,
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    modifier: Modifier = Modifier,
    currentPage: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    val isFirstPage = currentPage == 0
    val isLastPage = currentPage == totalPages - 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                modifier = Modifier.visible(!isFirstPage),
                onClick = onBackClick,
            ) {
                Icon(
                    modifier = Modifier.padding(end = 4.dp).size(18.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
                Text("Back")
            }

            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onSkipClick,
            ) {
                Text(
                    if (isLastPage) "Done" else "Skip",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Button(
                modifier = Modifier.visible(!isLastPage),
                onClick = onNextClick,
            ) {
                Text("Next")
                Icon(
                    modifier = Modifier.padding(start = 4.dp).size(18.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                )
            }
        }
    }
}

@Composable
private fun PageIndicators(
    modifier: Modifier = Modifier,
    currentPage: Int,
    totalPages: Int,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { page ->
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingPreviewPage1() {
    ToptyTheme {
        Surface {
            Onboarding(
                uiState = OnboardingUiState(currentPage = 0),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingPreviewPage2() {
    ToptyTheme {
        Surface {
            Onboarding(
                uiState = OnboardingUiState(currentPage = 1),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingPreviewPage3() {
    ToptyTheme {
        Surface {
            Onboarding(
                uiState = OnboardingUiState(currentPage = 2),
                onEvent = {}
            )
        }
    }
}
