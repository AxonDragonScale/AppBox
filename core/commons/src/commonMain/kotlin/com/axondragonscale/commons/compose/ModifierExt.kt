package com.axondragonscale.commons.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

/**
 * Conditionally applies the given [modifier] if [condition] is true.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier = if (condition) this.modifier() else this

/**
 * Conditionally applies [ifTrue] modifier if [condition] is true,
 * otherwise applies [ifFalse] modifier.
 */
inline fun Modifier.thenIfElse(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: Modifier.() -> Modifier
): Modifier = if (condition) this.ifTrue() else this.ifFalse()

/**
 * Conditionally applies the given [modifier] if [value] is not null.
 */
inline fun <T> Modifier.thenIfNotNull(
    value: T?,
    modifier: Modifier.(T) -> Modifier
): Modifier = if (value != null) this.modifier(value) else this

/**
 * Applies alpha to the composable based on [isVisible].
 */
fun Modifier.visible(isVisible: Boolean) = this.alpha(if (isVisible) 1f else 0f)
