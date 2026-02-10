package com.axondragonscale.topty.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.axondragonscale.commons.collectWithLifecycle
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.onboarding.pages.FeaturesPage
import com.axondragonscale.topty.ui.onboarding.pages.SetPasswordPage
import com.axondragonscale.topty.ui.onboarding.pages.WelcomePage
import com.axondragonscale.topty.ui.theme.DefaultShape
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
        }
    }

    Onboarding(
        modifier = modifier,
        uiState = uiState,
        onEvent = { viewModel.onEvent(it) }
    )
}

@Composable
private fun Onboarding(
    modifier: Modifier = Modifier,
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        bottomBar = {
            OnboardingBottomBar(
                enabled = when (uiState.currentPage) {
                    2 -> uiState.password.isNotEmpty() &&
                         uiState.confirmPassword.isNotEmpty() &&
                         uiState.password == uiState.confirmPassword &&
                         uiState.passwordError == null
                    else -> true
                },
                onContinueClick = {
                    when (uiState.currentPage) {
                        2 -> onEvent(OnboardingUiEvent.SetPassword)
                        else -> onEvent(OnboardingUiEvent.NextPage)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display page based on current page (no swiping)
            when (uiState.currentPage) {
                0 -> WelcomePage()
                1 -> FeaturesPage()
                2 -> SetPasswordPage(
                    password = uiState.password,
                    confirmPassword = uiState.confirmPassword,
                    passwordError = uiState.passwordError,
                    passwordStrength = uiState.passwordStrength,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onContinueClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onContinueClick,
            shape = DefaultShape,
            enabled = enabled,
        ) {
            Text("Continue")
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
