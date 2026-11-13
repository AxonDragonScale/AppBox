package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun AlbumNameDialog(
    modifier: Modifier = Modifier,
    currentAlbumName: String? = null,
    title: String = "New album",
    helper: String? = "You can rename it later.",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    WallmaticOverlay(onDismiss = onDismiss) {
        AlbumNameDialogContent(
            modifier = modifier.align(Alignment.Center),
            currentAlbumName = currentAlbumName,
            title = title,
            helper = helper,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun AlbumNameDialogContent(
    modifier: Modifier = Modifier,
    currentAlbumName: String? = null,
    title: String = "New album",
    helper: String? = "You can rename it later.",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var albumName by rememberSaveable { mutableStateOf(currentAlbumName.orEmpty()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .background(MaterialTheme.wallmatic.paper)
            .border(1.dp, MaterialTheme.wallmatic.ink)
            .padding(20.dp),
    ) {
        Text(
            text = title,
            style = WallmaticTextStyles.dialogTitle,
            color = MaterialTheme.wallmatic.ink,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ALBUM NAME",
            style = WallmaticTextStyles.microStrong,
            color = MaterialTheme.wallmatic.inkMuted,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, MaterialTheme.wallmatic.ink)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = albumName,
                onValueChange = { albumName = it },
                singleLine = true,
                textStyle = WallmaticTextStyles.body.copy(
                    color = MaterialTheme.wallmatic.ink,
                ),
                cursorBrush = SolidColor(MaterialTheme.wallmatic.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (albumName.isNotBlank()) {
                            onConfirm(albumName.trim())
                            onDismiss()
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }

        if (helper != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = helper,
                style = WallmaticTextStyles.caption.copy(fontSize = 10.sp, lineHeight = 12.sp),
                color = MaterialTheme.wallmatic.inkMuted,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.End),
        ) {
            TextAction(
                text = "Cancel",
                onClick = onDismiss,
                color = MaterialTheme.wallmatic.inkMuted,
                emphasized = false,
                compact = true,
            )
            TextAction(
                text = "Create",
                onClick = {
                    onConfirm(albumName.trim())
                    onDismiss()
                },
                enabled = albumName.isNotBlank(),
                compact = true,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AlbumNameDialogPreview() {
    WallmaticTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.wallmatic.paper),
            contentAlignment = Alignment.Center,
        ) {
            AlbumNameDialogContent(
                currentAlbumName = null,
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
