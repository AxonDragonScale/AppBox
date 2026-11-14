package com.axondragonscale.storageviewer.data

import android.util.Xml
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferenceType
import org.xmlpull.v1.XmlPullParser
import java.io.FileInputStream

/** Created by Ronak on 14/11/26 */

private const val MAP_TAG = "map"
private const val SET_TAG = "set"
private const val NULL_TAG = "null"
private const val NAME_ATTRIBUTE = "name"
private const val VALUE_ATTRIBUTE = "value"

// What the file recorded for an explicit null, which no preference value can otherwise express.
private const val NULL_TEXT = "null"

/**
 * Reads one SharedPreferences file as XML. `Context.getSharedPreferences` is never used: it would
 * create the directory, keep a cached map and rewrite the whole file on a later flush.
 */
internal object SharedPrefsReader {

    fun entries(path: String): List<PreferenceEntry> = FileInputStream(path).use { input ->
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)
        read(parser)
    }
}

// Malformed XML throws out of the parser, so a broken file reports unreadable instead of a short list.
private fun read(parser: XmlPullParser): List<PreferenceEntry> {
    mapRoot(parser)
    val entries = mutableListOf<PreferenceEntry>()
    var event = parser.next()
    while (event != XmlPullParser.END_DOCUMENT) {
        if (event == XmlPullParser.START_TAG && parser.depth == 2) {
            element(parser)?.let { entry -> entries += entry }
        }
        event = parser.next()
    }
    return entries.sortedBy { entry -> entry.key }
}

private fun mapRoot(parser: XmlPullParser) {
    parser.nextTag()
    if (parser.name != MAP_TAG) {
        throw IllegalStateException("Expected <$MAP_TAG> root, found <${parser.name}>")
    }
}

private fun element(parser: XmlPullParser): PreferenceEntry? {
    val tag = parser.name.orEmpty()
    val key = parser.getAttributeValue(null, NAME_ATTRIBUTE) ?: return null
    val type = when (tag) {
        "string" -> PreferenceType.STRING
        "int" -> PreferenceType.INT
        "long" -> PreferenceType.LONG
        "float" -> PreferenceType.FLOAT
        "double" -> PreferenceType.DOUBLE
        "boolean" -> PreferenceType.BOOL
        SET_TAG -> PreferenceType.STRING_SET
        NULL_TAG -> PreferenceType.STRING
        // `<map>`, `<list>`, `<int-array>` and `<string-array>`: written by XmlUtils.writeValueXml
        // for values an Editor cannot produce, so they are rendered rather than rejected.
        else -> PreferenceType.BYTES
    }
    val value = when (tag) {
        "string" -> elementText(parser)
        SET_TAG -> setText(parser)
        NULL_TAG -> NULL_TEXT
        else -> parser.getAttributeValue(null, VALUE_ATTRIBUTE) ?: elementText(parser)
    }
    val display = displayedText(value)
    return PreferenceEntry(key, type, display.text, display.byteCount, display.truncated)
}

// Accumulated instead of nextText(): a text split by an entity or CDATA section throws there.
private fun elementText(parser: XmlPullParser): String {
    val text = StringBuilder()
    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType == XmlPullParser.TEXT || parser.eventType == XmlPullParser.CDSECT) {
            text.append(parser.text.orEmpty())
        }
    }
    return text.toString()
}

private fun setText(parser: XmlPullParser): String {
    val values = mutableListOf<String>()
    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType == XmlPullParser.START_TAG) values += elementText(parser)
    }
    return setDisplay(values)
}
