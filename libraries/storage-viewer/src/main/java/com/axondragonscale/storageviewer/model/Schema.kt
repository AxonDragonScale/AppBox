package com.axondragonscale.storageviewer.model

/** Created by Ronak on 14/11/26 */

public data class DatabaseTable(
    val name: String,
    val isView: Boolean,
    val rowCount: Long,
    val columnCount: Int,
)

public data class TableSchema(
    val sql: String?,
    val columns: List<SchemaColumn>,
    val indexes: List<SchemaIndex>,
)

public data class SchemaColumn(
    val name: String,
    val type: String,
    val notNull: Boolean,
    val primaryKey: Boolean,
    val default: String?,
)

public data class SchemaIndex(
    val name: String,
    val unique: Boolean,
    val columns: List<String>,
)
