package com.axondragonscale.logcatviewer.data

import com.axondragonscale.logcatviewer.model.LogBuffer

/** Created by Ronak on 09/11/26 */

/**
 * The platform client as this app's UID. No `-T` count, so it seeds from the whole buffer and then
 * follows; no `-b` unless a buffer is chosen, which leaves the default buffers in place.
 */
class ProcessLogcatSource : ILogcatSource {
    override fun open(buffer: LogBuffer): ILogcatProcess {
        val args = buildList {
            add("logcat")
            add("-v")
            add(THREADTIME_FORMAT)
            addAll(buffer.args)
        }
        val process = ProcessBuilder(args).start()
        return object : ILogcatProcess {
            override val input get() = process.inputStream
            override val error get() = process.errorStream
            override fun close() = process.destroy()
        }
    }
}
