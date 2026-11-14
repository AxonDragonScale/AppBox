package com.axondragonscale.storageviewer.data

import android.database.Cursor
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteReadOnlyDatabaseException
import com.axondragonscale.storageviewer.model.Cell
import com.axondragonscale.storageviewer.model.CellKind
import com.axondragonscale.storageviewer.model.DatabaseStore
import com.axondragonscale.storageviewer.model.DatabaseTable
import com.axondragonscale.storageviewer.model.SchemaColumn
import com.axondragonscale.storageviewer.model.SchemaIndex
import com.axondragonscale.storageviewer.model.TableRow
import com.axondragonscale.storageviewer.model.TableRows
import com.axondragonscale.storageviewer.model.TableSchema
import java.io.File

/** Created by Ronak on 14/11/26 */

private const val TABLE_COUNT_QUERY =
    "SELECT count(*) FROM sqlite_master WHERE type IN ('table', 'view') AND name NOT LIKE 'sqlite_%'"

private const val TABLE_LIST_QUERY =
    "SELECT name, type FROM sqlite_master WHERE type IN ('table', 'view') " +
        "AND name NOT LIKE 'sqlite_%' ORDER BY name"

private const val HEX_DIGITS = "0123456789ABCDEF"

/**
 * Blocking reads over one SQLite file. Callers open, read and close inside
 * `withContext(Dispatchers.IO)`; nothing is kept between calls.
 */
internal object DatabaseReader {

    fun open(path: String): SQLiteDatabase =
        try {
            SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
        } catch (_: SQLiteCantOpenDatabaseException) {
            openWritable(path)
        } catch (_: SQLiteReadOnlyDatabaseException) {
            openWritable(path)
        }

    // A live WAL file needs a writable -shm, which a read-only open cannot supply. The fallback
    // handle is pinned to query_only so it still rejects every write; the file is never created.
    private fun openWritable(path: String): SQLiteDatabase {
        val database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        return try {
            database.execSQL("PRAGMA query_only=ON")
            database.execSQL("PRAGMA busy_timeout=1500")
            database
        } catch (e: SQLiteException) {
            database.close()
            throw e
        }
    }

    fun describe(store: DatabaseStore): DatabaseStore = open(store.path).use { database ->
        val userVersion = database.rawQuery("PRAGMA user_version", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        val tableCount = database.rawQuery(TABLE_COUNT_QUERY, null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        store.copy(
            sizeBytes = maxOf(store.sizeBytes, File(store.path).length()),
            tableCount = tableCount,
            userVersion = userVersion,
        )
    }

    fun tables(store: DatabaseStore): List<DatabaseTable> = open(store.path).use { database ->
        database.rawQuery(TABLE_LIST_QUERY, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(0).orEmpty()
                    add(table(database, name, isView = cursor.getString(1) == "view"))
                }
            }
        }
    }

    fun schema(store: DatabaseStore, table: String): TableSchema = open(store.path).use { database ->
        val quoted = quote(table)
        TableSchema(
            sql = schemaSql(database, table),
            columns = columns(database, quoted),
            indexes = indexes(database, quoted),
        )
    }

    fun rows(store: DatabaseStore, table: String, limit: Int): TableRows = open(store.path).use { database ->
        val quoted = quote(table)
        val total = database.rawQuery("SELECT COUNT(*) FROM $quoted", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        }
        TableRows(rows = page(database, quoted, limit), total = total)
    }

    private fun table(database: SQLiteDatabase, name: String, isView: Boolean): DatabaseTable {
        val quoted = quote(name)
        val rowCount = try {
            database.rawQuery("SELECT COUNT(*) FROM $quoted", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            }
        } catch (_: SQLiteException) {
            -1L
        }
        val columnCount = try {
            database.rawQuery("PRAGMA table_info($quoted)", null).use { cursor -> cursor.count }
        } catch (_: SQLiteException) {
            0
        }
        return DatabaseTable(name = name, isView = isView, rowCount = rowCount, columnCount = columnCount)
    }

    private fun schemaSql(database: SQLiteDatabase, table: String): String? =
        database.rawQuery("SELECT sql FROM sqlite_master WHERE name = ?", arrayOf(table)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private fun columns(database: SQLiteDatabase, quoted: String): List<SchemaColumn> =
        database.rawQuery("PRAGMA table_info($quoted)", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        SchemaColumn(
                            name = cursor.getString(1).orEmpty(),
                            type = cursor.getString(2).orEmpty(),
                            notNull = cursor.getInt(3) != 0,
                            primaryKey = cursor.getInt(5) > 0,
                            default = if (cursor.isNull(4)) null else cursor.getString(4),
                        ),
                    )
                }
            }
        }

    private fun indexes(database: SQLiteDatabase, quoted: String): List<SchemaIndex> =
        database.rawQuery("PRAGMA index_list($quoted)", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(1).orEmpty()
                    add(
                        SchemaIndex(
                            name = name,
                            unique = cursor.getInt(2) != 0,
                            columns = indexColumns(database, name),
                        ),
                    )
                }
            }
        }

    private fun indexColumns(database: SQLiteDatabase, index: String): List<String> =
        database.rawQuery("PRAGMA index_info(${quote(index)})", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val column = cursor.getString(2)
                    if (column != null) add(column)
                }
            }
        }

    // A view and a WITHOUT ROWID table reject `rowid`, so a failed page falls back to the ordinal.
    private fun page(database: SQLiteDatabase, quoted: String, limit: Int): List<TableRow> =
        try {
            read(database, "SELECT rowid, * FROM $quoted LIMIT $limit", hasRowid = true)
        } catch (_: SQLiteException) {
            read(database, "SELECT * FROM $quoted LIMIT $limit", hasRowid = false)
        }

    private fun read(database: SQLiteDatabase, sql: String, hasRowid: Boolean): List<TableRow> =
        database.rawQuery(sql, null).use { cursor ->
            buildList {
                var position = 0
                while (cursor.moveToNext()) {
                    position++
                    add(if (hasRowid) cursor.row(cursor.getInt(0), 1) else cursor.row(position, 0))
                }
            }
        }
}

private fun Cursor.row(id: Int, offset: Int): TableRow {
    var label: Cell? = null
    var fallback: Cell? = null
    val cells = buildList {
        for (index in offset until columnCount) {
            val cell = cell(index)
            add(cell)
            if (fallback == null && cell.kind != CellKind.NULL) fallback = cell
            if (label == null && cell.kind == CellKind.TEXT && !getString(index).isNullOrBlank()) label = cell
        }
    }
    val shown = label ?: fallback
    return TableRow(
        id = id,
        label = shown?.display.orEmpty(),
        labelKind = shown?.kind ?: CellKind.NULL,
        cells = cells,
    )
}

private fun Cursor.cell(index: Int): Cell {
    val column = getColumnName(index)
    return when (getType(index)) {
        Cursor.FIELD_TYPE_NULL -> Cell(column, CellKind.NULL, "NULL", 0, false)
        Cursor.FIELD_TYPE_INTEGER -> scalar(column, CellKind.INTEGER, getString(index))
        Cursor.FIELD_TYPE_FLOAT -> scalar(column, CellKind.REAL, getString(index))
        Cursor.FIELD_TYPE_BLOB -> blob(column, getBlob(index))
        else -> text(column, getString(index))
    }
}

private fun scalar(column: String, kind: CellKind, value: String?): Cell {
    val display = value.orEmpty()
    return Cell(column, kind, display, display.toByteArray(Charsets.UTF_8).size, false)
}

private fun text(column: String, value: String?): Cell {
    val display = displayedText(value.orEmpty())
    return Cell(column, CellKind.TEXT, display.text, display.byteCount, display.truncated)
}

private fun blob(column: String, value: ByteArray?): Cell {
    val full = value ?: ByteArray(0)
    val read = if (full.size <= BLOB_BYTES) full else full.copyOf(BLOB_BYTES)
    val truncated = full.size > BLOB_BYTES
    val display = buildString {
        append("X'")
        append(read.toHex())
        append(if (truncated) "…'" else "'")
    }
    return Cell(column, CellKind.BLOB, display, full.size, truncated)
}

private fun quote(identifier: String): String = "\"" + identifier.replace("\"", "\"\"") + "\""

private fun ByteArray.toHex(): String = buildString(size * 2) {
    for (byte in this@toHex) {
        val value = byte.toInt() and 0xFF
        append(HEX_DIGITS[value shr 4])
        append(HEX_DIGITS[value and 0x0F])
    }
}
