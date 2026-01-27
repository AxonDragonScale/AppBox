package com.axondragonscale.jest.ui.tune

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.appdump.jest.model.BlacklistFlag
import com.axondragonscale.appdump.jest.model.Category
import com.axondragonscale.appdump.jest.model.JokeType
import com.axondragonscale.appdump.jest.ui.component.MultiSelector
import com.axondragonscale.appdump.jest.ui.component.createMultiSelectorOptions
import com.axondragonscale.appdump.jest.ui.theme.JestTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Ronak Harkhani on 07/04/24
 */

@Composable
fun PrefsBottomSheet(
    modifier: Modifier = Modifier,
    vm: PrefsVM = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    PrefsBottomSheet(
        modifier = modifier,
        uiState = uiState,
        onEvent = { vm.onEvent(it) },
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefsBottomSheet(
    modifier: Modifier = Modifier,
    uiState: PrefsUiState,
    onEvent: (PrefsUiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
                .size(width = 48.dp, height = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                    shape = RoundedCornerShape(2.dp),
                ),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "Tell us your taste in Jokes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            MultiSelector(
                label = "CATEGORY",
                options = createMultiSelectorOptions(
                    options = Category.entries,
                    selectedOptions = uiState.selectedCategories
                ),
                onSelectionChanged = { onEvent(PrefsUiEvent.CategorySelectionChanged(it)) }
            )

            // TODO: Add Language Selector here instead of this

            MultiSelector(
                modifier = Modifier.padding(top = 16.dp),
                label = "JOKE TYPE",
                options = createMultiSelectorOptions(
                    options = JokeType.entries,
                    selectedOptions = uiState.selectedJokeTypes
                ),
                onSelectionChanged = { onEvent(PrefsUiEvent.JokeTypeSelectionChanged(it)) }
            )

            MultiSelector(
                modifier = Modifier.padding(top = 16.dp),
                label = "BLACKLIST",
                options = createMultiSelectorOptions(
                    options = BlacklistFlag.entries,
                    selectedOptions = uiState.selectedBlacklistFlags
                ),
                onSelectionChanged = { onEvent(PrefsUiEvent.BlacklistFlagSelectionChanged(it)) }
            )
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrefsBottomSheetPreview() {
    JestTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            PrefsBottomSheet(
                uiState = PrefsUiState(),
                onEvent = { },
                onDismiss = { }
            )
        }
    }
}
