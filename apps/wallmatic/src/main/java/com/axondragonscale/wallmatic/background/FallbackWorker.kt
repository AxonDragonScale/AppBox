package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Periodic safety net: re-evaluates the rule in case the tick alarm was dropped. */
class FallbackWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val updateCoordinator: UpdateCoordinator by inject()

    override suspend fun doWork(): Result {
        updateCoordinator.applyIfDue()
        return Result.success()
    }

}
