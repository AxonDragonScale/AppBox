package com.axondragonscale.logcatviewer.model

/** Created by Ronak on 09/11/26 */

enum class LogLevel(val letter: Char, val label: String) {
    VERBOSE('V', "Verbose"),
    DEBUG('D', "Debug"),
    INFO('I', "Info"),
    WARN('W', "Warn"),
    ERROR('E', "Error"),
    FATAL('F', "Fatal");

    companion object {
        internal fun from(letter: Char): LogLevel? = entries.firstOrNull { it.letter == letter }

        val all: Set<LogLevel> = entries.toSet()
    }
}

enum class LogBuffer(val label: String, val caption: String, val args: List<String>) {
    ALL("all", "all buffers", emptyList()),
    MAIN("main", "main buffer", listOf("-b", "main")),
    SYSTEM("system", "system buffer", listOf("-b", "system")),
    CRASH("crash", "crash buffer", listOf("-b", "crash")),
    EVENTS("events", "events buffer", listOf("-b", "events")),
}

data class LogEntry(
    val id: Long,
    val time: String,
    val pid: Int,
    val tid: Int,
    val level: LogLevel,
    val tag: String,
    val message: String,
) {
    val firstLine: String get() = message.substringBefore('\n')

    val trace: List<String> get() = message.lineSequence().drop(1).toList()

    fun asLine(): String = "$time ${level.letter} $tag: $message"
}
