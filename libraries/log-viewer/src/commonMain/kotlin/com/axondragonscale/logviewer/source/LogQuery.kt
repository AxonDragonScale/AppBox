package com.axondragonscale.logviewer.source

import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogRecord

/** Created by Ronak Harkhani on 11/22/26 */

/** A record field a filter rule can name. */
enum class LogFacet { TAG, PACKAGE }

/** Keeps every record carrying [value] in [facet], or drops them. */
data class LogRule(val facet: LogFacet, val value: String, val include: Boolean)

/**
 * What the viewer is asking for. The source applies it however it can: SQL, or in memory.
 *
 * [rules] is the single filter vocabulary, so every seam between the viewer and a source carries
 * the same shape whatever facets exist.
 */
data class LogQuery(
    val levels: Set<LogLevel> = LogLevel.entries.toSet(),
    /** Matched against the tag and the message, case-insensitively. */
    val text: String = "",
    val rules: List<LogRule> = emptyList(),
) {

    private val includedTags = rules.including(LogFacet.TAG)
    private val excludedTags = rules.excluding(LogFacet.TAG)
    private val includedPackages = rules.including(LogFacet.PACKAGE)
    private val excludedPackages = rules.excluding(LogFacet.PACKAGE)

    val isFiltered: Boolean
        get() = levels.size < LogLevel.entries.size || text.isNotBlank() || rules.isNotEmpty()

    fun included(facet: LogFacet): Set<String> = when (facet) {
        LogFacet.TAG -> includedTags
        LogFacet.PACKAGE -> includedPackages
    }

    fun excluded(facet: LogFacet): Set<String> = when (facet) {
        LogFacet.TAG -> excludedTags
        LogFacet.PACKAGE -> excludedPackages
    }

    /** Adds [rule], dropping the same value from the other side so one value never both keeps and drops. */
    fun withRule(rule: LogRule): LogQuery =
        copy(rules = rules.filterNot { it.facet == rule.facet && it.value == rule.value } + rule)

    fun withoutRule(facet: LogFacet, value: String): LogQuery =
        copy(rules = rules.filterNot { it.facet == facet && it.value == value })

    /** The filter for a source that has no query language of its own. */
    fun matches(record: LogRecord): Boolean {
        val packageName = record.meta.packageName
        return record.level in levels &&
            record.tag !in excludedTags &&
            packageName !in excludedPackages &&
            (includedTags.isEmpty() || record.tag in includedTags) &&
            (includedPackages.isEmpty() || packageName in includedPackages) &&
            (
                text.isBlank() ||
                    record.tag.contains(text, ignoreCase = true) ||
                    record.message.contains(text, ignoreCase = true)
                )
    }

    private fun List<LogRule>.including(facet: LogFacet): Set<String> =
        filter { it.facet == facet && it.include }.mapTo(mutableSetOf()) { it.value }

    private fun List<LogRule>.excluding(facet: LogFacet): Set<String> =
        filter { it.facet == facet && !it.include }.mapTo(mutableSetOf()) { it.value }
}

/**
 * The values a source can enumerate for its filter pickers. A source that cannot enumerate one
 * facet leaves it empty; the picker still accepts a typed value.
 */
data class LogFacets(
    val tags: List<String> = emptyList(),
    val packages: List<String> = emptyList(),
) {

    fun values(facet: LogFacet): List<String> = when (facet) {
        LogFacet.TAG -> tags
        LogFacet.PACKAGE -> packages
    }

    /** This enumeration plus whatever the loaded records already show. */
    fun withRecords(records: List<LogRecord>): LogFacets = LogFacets(
        tags = (tags + records.map { it.tag }).distinct().sorted(),
        packages = (packages + records.mapNotNull { it.meta.packageName }).distinct().sorted(),
    )
}
