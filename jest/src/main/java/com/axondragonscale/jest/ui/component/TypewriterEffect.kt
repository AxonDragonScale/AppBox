package com.axondragonscale.jest.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

/**
 * Created by Ronak Harkhani on 06/04/24
 */

@Composable
fun Modifier.typewriterEffect(
    text: String,
    onTextUpdate: (String) -> Unit,
    onEffectComplete: (suspend () -> Unit)? = null,
    chunkRange: IntRange = 1.rangeTo(2),
    delayRange: LongRange = 16L.rangeTo(64L),
): Modifier {
    LaunchedEffect(text) {
        if (text.isBlank()) return@LaunchedEffect

        var currentSize = 0
        while (currentSize < text.length) {
            val currentChunk = Random.nextInt(chunkRange)
            val currentDelay = Random.nextLong(delayRange)
            currentSize = minOf(currentSize + currentChunk, text.length)

//            val d = buildAnnotatedString {
//                append(text.take(currentSize))
//                append(
//                    AnnotatedString(
//                        text = text.takeLast(text.length - currentSize),
//                        spanStyle = SpanStyle(color = Color.Transparent)
//                    )
//                )
//            }

            val displayText = text.take(currentSize)
            onTextUpdate.invoke(displayText)

            delay(currentDelay)
        }
        onEffectComplete?.invoke()
    }

    return this
}
