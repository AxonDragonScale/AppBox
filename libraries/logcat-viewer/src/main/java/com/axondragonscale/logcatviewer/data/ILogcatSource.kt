package com.axondragonscale.logcatviewer.data

import com.axondragonscale.logcatviewer.model.LogBuffer
import java.io.InputStream

/** Created by Ronak on 09/11/26 */

/** The line shape the reader parses; a source must emit exactly this or every line continues above. */
internal const val THREADTIME_FORMAT = "threadtime"

/**
 * Where the viewer gets its lines.
 *
 * The stream must be `logcat -v threadtime` output — `MM-DD HH:MM:SS.mmm  PID  TID L TAG: message` —
 * because [com.axondragonscale.logcatviewer.stream.LogcatStreamer] parses that shape.
 *
 * The default [ProcessLogcatSource] runs `logcat` as the calling app, which is enough for the app's
 * own records on every supported Android version and needs no permission. A host that has another
 * identity (root, Shizuku, a remote device) can implement this interface and pass it in.
 */
interface ILogcatSource {
    fun open(buffer: LogBuffer): ILogcatProcess
}

/** One running `logcat` reader, abstracted so a source need not hand back a real [Process]. */
interface ILogcatProcess {
    val input: InputStream
    val error: InputStream
    fun close()
}
