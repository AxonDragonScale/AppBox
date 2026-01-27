package com.axondragonscale.jest.model

import android.content.Intent
import com.axondragonscale.jest.model.serialization.JokeSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

/**
 * Created by Ronak Harkhani on 02/04/24
 */
@Serializable(with = JokeSerializer::class)
sealed interface IJoke {
    val id: Int
    val lang: Language
    val category: Category
    val type: JokeType
    val flags: Flags
    val safe: Boolean
    var favorite: Boolean
    val timestamp: Long
}

fun IJoke.getFirstLine() = when (this) {
    is OnePartJoke -> this.joke
    is TwoPartJoke -> this.setup
}

fun IJoke.getSecondLine() = when (this) {
    is TwoPartJoke -> this.delivery
    else -> null
}

private fun IJoke.getShareableText() =
    """ 
    ${getFirstLine()}
    ${if (getSecondLine() != null) "\n${getSecondLine()}\n" else ""}
    - Brought to you by Jest, Get your daily dose of humour.
    """.trimIndent()

fun IJoke.getShareIntent(): Intent = Intent.createChooser(
    Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getShareableText())
    },
    null
)

fun IJoke.isOld() =
    timestamp < (System.currentTimeMillis() - 1.days.inWholeMilliseconds)

/**
 * Joke where the type is `single`
 */
@Serializable
data class OnePartJoke(
    override val id: Int,
    override val lang: Language,
    override val category: Category,
    override val flags: Flags,
    override val safe: Boolean,
    override var favorite: Boolean = false,
    override val timestamp: Long,
    override val type: JokeType,
    val joke: String,
) : IJoke

/**
 * Joke where type is `twopart`
 */
@Serializable
data class TwoPartJoke(
    override val id: Int,
    override val lang: Language,
    override val category: Category,
    override val safe: Boolean,
    override val flags: Flags,
    override var favorite: Boolean = false,
    override val timestamp: Long,
    override val type: JokeType,
    val setup: String,
    val delivery: String,
) : IJoke
