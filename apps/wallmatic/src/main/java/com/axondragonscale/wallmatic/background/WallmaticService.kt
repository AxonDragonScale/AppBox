package com.axondragonscale.wallmatic.background

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WallmaticService : Service(), KoinComponent {

    companion object {
        private const val TAG = "WallmaticService"

        fun getIntent(context: Context): Intent =
            Intent(context, WallmaticService::class.java)
    }

    private val wallpaperUpdater: WallpaperUpdater by inject()
    private val appPrefsRepository: AppPrefsRepository by inject()
    private val syncManager: SyncManager by inject()

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        this.logD { "onCreate" }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.logD { "onStartCommand" }

        CoroutineScope(Dispatchers.IO).launch {
            syncManager.syncAlbums()
            wallpaperUpdater.updateWallpaper()

            TAG.logD { "Stopping service" }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        this.logD { "onDestroy" }
        super.onDestroy()
    }

}
