package com.axondragonscale.npinner.ui.common

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.axondragonscale.npinner.R
import com.axondragonscale.npinner.ui.theme.NPinnerTheme
import com.axondragonscale.commons.openNotificationSettings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Created by Ronak Harkhani on 18/05/23
 */

private enum class RequestStatus {
    NONE, REQUESTED, COMPLETED
}

@Composable
fun NotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    
    var status by remember { mutableStateOf(RequestStatus.NONE) }
    
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        onPermissionResult = {
            if (status == RequestStatus.REQUESTED) status = RequestStatus.COMPLETED
        }
    )
    if (permissionState.status.isGranted) return
    
    val observer = remember {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && !permissionState.status.isGranted) {
                if (status == RequestStatus.NONE) status = RequestStatus.REQUESTED
                permissionState.launchPermissionRequest()
            }
        }
    }
    
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    
    var dialogDismissed by remember { mutableStateOf(false) }
    if (
        status == RequestStatus.COMPLETED &&
        !dialogDismissed &&
        !permissionState.status.isGranted
    ) {
        Dialog(onDismissRequest = { dialogDismissed = true }) {
            NotificationPermissionRationale(onDismiss = { dialogDismissed = true })
        }
    }
}

@Composable
private fun NotificationPermissionRationale(onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(32.dp)
                    .size(80.dp),
                painter = painterResource(id = R.drawable.ic_npinner_icon),
                contentDescription = "Pinned Notification",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            
            Text(
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                text = "Pin Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = "NPinner needs Notification Permission to pin notifications to the Notification Panel.\n Please allow notifications from settings.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val context = LocalContext.current
            Button(
                onClick = { context.openNotificationSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                shape = RoundedCornerShape(2.dp),
            ) {
                Text(
                    text = "OPEN SETTINGS",
                    fontWeight = FontWeight.Bold,
                )
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
            ) {
                Text(
                    text = "NO THANKS",
                    fontWeight = FontWeight.Bold,
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationPermissionRationalePreview() {
    NPinnerTheme {
        NotificationPermissionRationale({})
    }
}



