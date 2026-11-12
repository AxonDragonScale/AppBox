package com.axondragonscale.wallmatic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun HorizontalRule(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.wallmatic.ruleSoft,
    thickness: Dp = Dimens.rule,
) = Box(
    modifier = modifier
        .fillMaxWidth()
        .height(thickness)
        .background(color)
)
