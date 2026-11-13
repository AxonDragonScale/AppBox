package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Syncs albums with the filesystem, then repairs the config. Deferred, off the timed path. */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val updateCoordinator: UpdateCoordinator by inject()

    override suspend fun doWork(): Result {
        updateCoordinator.refresh()
        return Result.success()
    }

}
