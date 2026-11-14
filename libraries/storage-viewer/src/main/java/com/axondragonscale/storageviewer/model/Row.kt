package com.axondragonscale.storageviewer.model

/** Created by Ronak on 14/11/26 */

public enum class CellKind { NULL, INTEGER, REAL, TEXT, BLOB }

/** [display] is the formatted value; [byteCount] is its wire size, whether or not it was capped. */
public data class Cell(
    val column: String,
    val kind: CellKind,
    val display: String,
    val byteCount: Int,
    val truncated: Boolean,
)

/**
 * [id] is the row's ordinal in the result; [label] is its first readable text column and
 * [labelKind] the kind of the cell that label came from.
 */
public data class TableRow(
    val id: Int,
    val label: String,
    val labelKind: CellKind,
    val cells: List<Cell>,
)

/** [total] counts the table, not the page in [rows]. */
public data class TableRows(
    val rows: List<TableRow>,
    val total: Long,
)
