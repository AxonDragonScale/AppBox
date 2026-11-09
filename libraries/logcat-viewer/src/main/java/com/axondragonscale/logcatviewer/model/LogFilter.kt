package com.axondragonscale.logcatviewer.model

import kotlinx.serialization.Serializable

/** Created by Ronak on 09/11/26 */

enum class FilterKind(val label: String) {
    TAG("tag"),
    PACKAGE("package"),
}

/**
 * One include or exclude rule. The filter sheet, the applied chips, the saved-view summary and the
 * events that add or remove a rule all speak this instead of a set per dimension.
 */
data class FilterRule(
    val kind: FilterKind,
    val value: String,
    val include: Boolean,
) {
    val label: String
        get() = buildString {
            if (!include) append('−')
            if (kind == FilterKind.PACKAGE) append("pkg:")
            append(value)
        }
}

@Serializable
data class LogFilter(
    val levels: Set<LogLevel> = LogLevel.all,
    val includeTags: Set<String> = emptySet(),
    val excludeTags: Set<String> = emptySet(),
    val includePackages: Set<String> = emptySet(),
    val excludePackages: Set<String> = emptySet(),
    val query: String = "",
    val regex: Boolean = false,
) {
    val rules: List<FilterRule>
        get() = buildList {
            includeTags.forEach { add(FilterRule(FilterKind.TAG, it, include = true)) }
            includePackages.forEach { add(FilterRule(FilterKind.PACKAGE, it, include = true)) }
            excludeTags.forEach { add(FilterRule(FilterKind.TAG, it, include = false)) }
            excludePackages.forEach { add(FilterRule(FilterKind.PACKAGE, it, include = false)) }
        }

    /** Adds [rule], dropping the same value from the opposite polarity: a value is in or out. */
    fun withRule(rule: FilterRule): LogFilter {
        val value = rule.value.trim()
        if (value.isEmpty()) return this
        return when (rule.kind) {
            FilterKind.TAG -> if (rule.include) {
                copy(includeTags = includeTags + value, excludeTags = excludeTags - value)
            } else {
                copy(excludeTags = excludeTags + value, includeTags = includeTags - value)
            }

            FilterKind.PACKAGE -> if (rule.include) {
                copy(includePackages = includePackages + value, excludePackages = excludePackages - value)
            } else {
                copy(excludePackages = excludePackages + value, includePackages = includePackages - value)
            }
        }
    }

    fun withoutRule(rule: FilterRule): LogFilter = when (rule.kind) {
        FilterKind.TAG -> if (rule.include) {
            copy(includeTags = includeTags - rule.value)
        } else {
            copy(excludeTags = excludeTags - rule.value)
        }

        FilterKind.PACKAGE -> if (rule.include) {
            copy(includePackages = includePackages - rule.value)
        } else {
            copy(excludePackages = excludePackages - rule.value)
        }
    }

    fun withLevel(level: LogLevel, enabled: Boolean): LogFilter =
        copy(levels = if (enabled) levels + level else levels - level)

    fun withQuery(query: String): LogFilter = copy(query = query)

    fun withRegex(enabled: Boolean): LogFilter = copy(regex = enabled)

    val levelLabel: String
        get() = when {
            levels.isEmpty() -> "No levels"
            levels.size == LogLevel.entries.size -> "All levels"
            levels.size == 1 -> levels.first().label
            else -> "${levels.size} levels"
        }

    val hasFilters: Boolean
        get() = levels.size != LogLevel.entries.size ||
            includeTags.isNotEmpty() ||
            excludeTags.isNotEmpty() ||
            includePackages.isNotEmpty() ||
            excludePackages.isNotEmpty() ||
            query.isNotBlank()

    fun summary(includeRules: Boolean = true): String = buildList {
        if (levels.size != LogLevel.entries.size) add(levelLabel)
        if (includeRules) addAll(rules.map { it.label })
        if (query.isNotBlank()) add("\"$query\"")
    }.ifEmpty { listOf("Everything") }.joinToString(" · ")
}
