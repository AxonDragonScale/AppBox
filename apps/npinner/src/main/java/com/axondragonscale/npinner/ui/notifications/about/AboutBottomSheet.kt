package com.axondragonscale.npinner.ui.notifications.about

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.npinner.BuildConfig
import com.axondragonscale.npinner.NPinnerConstants
import com.axondragonscale.npinner.R
import com.axondragonscale.npinner.ui.common.NPinnerModalBottomSheet
import com.axondragonscale.npinner.ui.theme.NPinnerTheme

/**
 * Created by Ronak Harkhani on 13/05/23
 */

@Composable
fun AboutBottomSheet(onDismiss: () -> Unit) {
    NPinnerModalBottomSheet(onDismiss = onDismiss) {
        AboutContent()
    }
}

@Composable
fun AboutContent() {
    Column {
        AboutHeader()
        DeveloperInfo()
        GithubInfo()
        SourceCodeLink()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AboutHeader() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "NPINNER",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = "VERSION ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun DeveloperInfo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(16.dp),
            imageVector = Icons.Default.Person,
            contentDescription = "Developer",
        )

        Column {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = "Developer",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "Ronak Harkhani",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun GithubInfo() {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
            .clickable {
                uriHandler.openUri(NPinnerConstants.GITHUB_PROFILE_LINK)
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.padding(16.dp).size(24.dp),
            painter = painterResource(R.drawable.ic_github),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Column {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = "Github",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "AxonDragonScale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun SourceCodeLink() {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable {
                uriHandler.openUri(NPinnerConstants.GITHUB_REPO_LINK)
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(16.dp),
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = "Open Source Code",
        )

        Text(
            text = "Source Code",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ThemePickerBottomSheetPreview() {
    NPinnerTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            AboutContent()
        }
    }
}
