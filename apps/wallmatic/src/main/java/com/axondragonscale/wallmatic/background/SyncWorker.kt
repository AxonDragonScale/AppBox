package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.axondragonscale.wallmatic.core.SyncManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Refreshes albums against the filesystem. Deferred, so the timed path never waits on SAF. */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val syncManager: SyncManager by inject()

    override suspend fun doWork(): Result {
        syncManager.syncAlbums()
        return Result.success()
    }

}
