package com.axondragonscale.jest.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.JestTheme

@Composable
fun JestTag(tag: String) {
    Text(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(8.dp)
            )
            .defaultMinSize(minHeight = 24.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .wrapContentHeight(align = Alignment.CenterVertically),
        text = tag.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JestTagPreview() {
    JestTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(32.dp),
        ) {
            JestTag("TAG")
        }
    }
}
