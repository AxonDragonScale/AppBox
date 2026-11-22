package com.axondragonscale.commons.devtools.logs.data

import com.axondragonscale.commons.LogPriority
import com.axondragonscale.commons.Logger
import com.axondragonscale.commons.logE
import com.axondragonscale.logviewer.source.LogSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 11/21/26
 *
 * Copies every [Logger] record into a Room database beside the platform log, so a session can be
 * read after logcat has rolled over. A host builds the database for its platform and hands it over
 * with `installDevLogStore`; everything below is shared.
 */
internal object DevLogStore {

    private const val KEEP_ROWS = 20_000
    private const val BATCH_SIZE = 100
    private const val QUEUE_SIZE = 1_000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val queue = Channel<DevLogEntity>(QUEUE_SIZE, BufferOverflow.DROP_OLDEST)
    private val session = Random.nextLong().toULong().toString(16)

    @Volatile
    private var started = false

    @Volatile
    private var dao: DevLogDao? = null

    val isInstalled: Boolean get() = started

    /** The store as the shared log viewer expects it; reads stay empty until the database is open. */
    fun source(): LogSource = StoredLogSource { dao }

    /**
     * Opens the store on IO, trims it and starts capturing. The sink is registered immediately so
     * nothing after this call is missed, but records that arrive before the database is ready are
     * dropped rather than buffered.
     */
    fun install(databaseFactory: () -> DevLogDatabase) {
        if (started) return
        started = true
        Logger.addSink(::append)
        scope.launch {
            val database = runCatching { databaseFactory() }
                .onFailure { "DevLogStore".logE { "open failed: $it" } }
                .getOrNull() ?: return@launch
            val logDao = database.logDao()
            dao = logDao
            runCatching { logDao.trimToNewest(KEEP_ROWS) }
            drain(logDao)
        }
    }

    /** Never blocks the caller: the writer coroutine inserts whatever is queued. */
    private fun append(priority: LogPriority, tag: String, message: String) {
        if (dao == null) return
        queue.trySend(
            DevLogEntity(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                priority = priority.name,
                tag = tag,
                message = message,
                session = session,
            )
        )
    }

    private suspend fun drain(logDao: DevLogDao) {
        val batch = ArrayList<DevLogEntity>(BATCH_SIZE)
        while (true) {
            // The first record is waited for; whatever else is already queued joins it, so a quiet
            // app still sees one record land immediately and a burst becomes one insert.
            batch += queue.receive()
            while (batch.size < BATCH_SIZE) {
                batch += queue.tryReceive().getOrNull() ?: break
            }
            // Never log a log failure: it would feed this same sink and loop.
            runCatching { logDao.insert(batch) }
            batch.clear()
        }
    }
}
