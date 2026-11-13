package com.axondragonscale.wallmatic.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.util.Size
import android.view.WindowManager
import androidx.core.net.toUri
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.toBitmap
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.commons.logW
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.getRandomWallpaperOrNull
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.configValue
import com.axondragonscale.wallmatic.util.nextUpdate
import com.axondragonscale.wallmatic.util.resolvesFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by Ronak Harkhani on 23/09/24
 */
class WallpaperUpdater(
    val context: Context,
    private val appPrefsRepository: AppPrefsRepository,
    private val repository: WallmaticRepository,
) {

    private val manager = WallpaperManager.getInstance(context)

    /**
     * Update one or both wallpapers if the updateInterval has passed since the last update.
     * Determines target screen based on config and delegates to the TargetScreen overload.
     *
     * This is the only overload that honours the update interval.
     *
     * @return [WallpaperUpdateResult.NotDue] if the interval has not elapsed,
     * [WallpaperUpdateResult.Skipped] if the config is not usable, otherwise the result of the
     * delegated update.
     */
    suspend fun updateWallpaper(): WallpaperUpdateResult {
        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update skipped. Config is null" }
            return WallpaperUpdateResult.Skipped("Config is null")
        }

        if (!config.isInit) {
            this.logD { "Wallpaper update skipped. isInit: ${config.isInit}" }
            return WallpaperUpdateResult.Skipped("Config is not initialised")
        }

        val now = System.currentTimeMillis()
        val updateHome = config.homeConfig.autoCycleEnabled && config.nextUpdate < now
        val updateLock = config.lockConfig.autoCycleEnabled && config.nextUpdate < now

        val target = when {
            updateHome && (updateLock || config.mirrorHomeConfigForLock) -> TargetScreen.Both
            updateHome -> TargetScreen.Home
            updateLock -> TargetScreen.Lock
            else -> null
        } ?: run {
            this.logD { "Wallpaper update skipped. Next update not due yet." }
            return WallpaperUpdateResult.NotDue
        }

        return updateWallpaper(target)
    }

    /**
     * Updates the wallpaper for the given target, bypassing the update interval.
     * Note: If mirroring is enabled, this will update both the home and lock wallpapers.
     *
     * Config is only persisted for targets that were actually applied: `currentWallpaperId` is
     * written per applied screen, and `lastUpdated` advances as soon as one screen was applied so
     * a screen that rejects the change cannot block the cadence. A target that fails is reported
     * to the caller instead of being recorded as a success.
     *
     * A target whose album has no whitelisted wallpaper is not a failure: it is skipped and the
     * other target is still attempted. A [WallpaperUpdateResult.Failed] cause always wins over a
     * skip reason, and [WallpaperUpdateResult.Failed] is reserved for an apply that actually
     * failed.
     */
    suspend fun updateWallpaper(target: TargetScreen): WallpaperUpdateResult {
        this.logD { "Wallpaper update start. target: $target" }

        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update skipped. Config is null" }
            return WallpaperUpdateResult.Skipped("Config is null")
        }

        if (!config.isInit) {
            this.logD { "Wallpaper update skipped. isInit: ${config.isInit}" }
            return WallpaperUpdateResult.Skipped("Config is not initialised")
        }

        val timestamp = System.currentTimeMillis()
        val homeAlbumId = config.homeConfig.albumId
        val lockAlbumId = config.lockConfig.albumId
        val homeAlbum = repository.getFullAlbum(homeAlbumId)
        val lockAlbum = if (config.mirrorHomeConfigForLock) homeAlbum
                        else repository.getFullAlbum(lockAlbumId)

        if (homeAlbum == null || lockAlbum == null) {
            this.logD { "Albums not found. home: $homeAlbumId, lock: $lockAlbumId" }
            return WallpaperUpdateResult.Skipped("Album not found. home: $homeAlbumId, lock: $lockAlbumId")
        }

        // When mirroring, always apply home wallpaper to both screens regardless of target.
        val applyHome = target.isHome() || config.mirrorHomeConfigForLock
        val applyLock = target.isLock() && !config.mirrorHomeConfigForLock

        var homeWallpaperId: Int? = null
        var lockWallpaperId: Int? = null
        var applied = false
        var firstFailure: Throwable? = null
        var firstFailedTarget: TargetScreen? = null
        var skipReason: String? = null

        if (applyHome) {
            this.logD { "Updating home wallpaper. homeAlbumId: $homeAlbumId" }
            val wallpaper = homeAlbum.getRandomWallpaperOrNull()
            if (wallpaper == null) {
                // Every wallpaper in the album is blacklisted: nothing to apply, but not a failure.
                this.logW { "Home wallpaper skipped. No whitelisted wallpaper. homeAlbumId: $homeAlbumId" }
                skipReason = skipReason ?: "No whitelisted wallpaper in album. homeAlbumId: $homeAlbumId"
            } else {
                val applyTarget = if (config.mirrorHomeConfigForLock) TargetScreen.Both else TargetScreen.Home
                setWallpaper(wallpaper, applyTarget)
                    .onSuccess {
                        homeWallpaperId = wallpaper.id
                        if (config.mirrorHomeConfigForLock) lockWallpaperId = wallpaper.id
                        applied = true
                    }
                    .onFailure { e ->
                        firstFailure = firstFailure ?: e
                        firstFailedTarget = firstFailedTarget ?: applyTarget
                    }
            }
        }
        if (applyLock) {
            this.logD { "Updating lock wallpaper. lockAlbumId: $lockAlbumId" }
            val wallpaper = lockAlbum.getRandomWallpaperOrNull()
            if (wallpaper == null) {
                this.logW { "Lock wallpaper skipped. No whitelisted wallpaper. lockAlbumId: $lockAlbumId" }
                skipReason = skipReason ?: "No whitelisted wallpaper in album. lockAlbumId: $lockAlbumId"
            } else {
                setWallpaper(wallpaper, TargetScreen.Lock)
                    .onSuccess {
                        lockWallpaperId = wallpaper.id
                        applied = true
                    }
                    .onFailure { e ->
                        firstFailure = firstFailure ?: e
                        firstFailedTarget = firstFailedTarget ?: TargetScreen.Lock
                    }
            }
        }

        // Persist nothing unless a screen applied: the wallpaper did not change, so the cycle
        // must stay due.
        val cause = firstFailure
        if (!applied) {
            if (cause != null) {
                this.logE { "Wallpaper update failed. Nothing applied. cause: ${cause.message}" }
                // The Home screen reports the failure; the schedule itself stays due.
                appPrefsRepository.updateConfig { current ->
                    current.copy {
                        lastFailedAt = timestamp
                        failedTarget = (firstFailedTarget ?: target).configValue
                    }
                }
                return WallpaperUpdateResult.Failed(cause)
            }

            val reason = skipReason ?: "No target applied and no failure recorded. target: $target"
            this.logW { "Wallpaper update skipped. Nothing applied. reason: $reason" }
            return WallpaperUpdateResult.Skipped(reason)
        }

        appPrefsRepository.updateConfig { current ->
            current.copy {
                homeWallpaperId?.let { id ->
                    homeConfig = homeConfig.copy { currentWallpaperId = id }
                }
                lockWallpaperId?.let { id ->
                    lockConfig = lockConfig.copy { currentWallpaperId = id }
                }
                lastUpdated = timestamp

                when {
                    cause != null -> {
                        lastFailedAt = timestamp
                        failedTarget = (firstFailedTarget ?: target).configValue
                    }
                    current.resolvesFailure(target) -> {
                        lastFailedAt = -1
                        failedTarget = 0
                    }
                }
            }
        }
        if (cause == null) {
            this.logD { "Wallpaper update complete" }
            return WallpaperUpdateResult.Updated
        }

        this.logE { "Wallpaper update partially applied. cause: ${cause.message}" }
        return WallpaperUpdateResult.Failed(cause)
    }

    /**
     * Applies [wallpaper] to [target] and records it as current, exactly as a cycle apply does.
     * Only the wallpaper selection is bypassed, so the next tick stays a full interval away.
     */
    suspend fun applyWallpaper(wallpaper: Wallpaper, target: TargetScreen): WallpaperUpdateResult {
        val timestamp = System.currentTimeMillis()
        val cause = setWallpaper(wallpaper, target).exceptionOrNull()
        if (cause != null) {
            this.logE { "Wallpaper re-apply failed. wallpaperId: ${wallpaper.id}, cause: ${cause.message}" }
            return WallpaperUpdateResult.Failed(cause)
        }

        appPrefsRepository.updateConfig { current ->
            current.copy {
                if (target.isHome()) homeConfig = homeConfig.copy { currentWallpaperId = wallpaper.id }
                if (target.isLock()) lockConfig = lockConfig.copy { currentWallpaperId = wallpaper.id }
                lastUpdated = timestamp
            }
        }
        this.logD { "Wallpaper re-applied. wallpaperId: ${wallpaper.id}, target: $target" }
        return WallpaperUpdateResult.Updated
    }

    /**
     * Applies [wallpaper] to [target]. Failures are logged and returned, never thrown.
     *
     * A wallpaper without a custom crop is streamed straight to the system, which does its own
     * crop. A custom crop is decoded, cropped with [viewport] and applied as a bitmap; if any step
     * of that fails, the stream path is used instead so the wallpaper is still set.
     */
    suspend fun setWallpaper(wallpaper: Wallpaper, target: TargetScreen): Result<Unit> = runCatching {
        if (wallpaper.hasCustomCrop) {
            applyCropped(wallpaper, target)
        } else {
            applyStream(wallpaper, target)
        }
        Unit
    }.onFailure { e ->
        this.logE { "Setting wallpaper failed. wallpaperId: ${wallpaper.id}, target: $target, error: ${e.message}" }
    }

    private fun applyStream(wallpaper: Wallpaper, target: TargetScreen) {
        val stream = context.contentResolver.openInputStream(wallpaper.uri.toUri())
            ?: error("Cannot open wallpaper. uri: ${wallpaper.uri}")
        stream.use { manager.setStream(it, null, true, target.toFlag()) }
    }

    private suspend fun applyCropped(wallpaper: Wallpaper, target: TargetScreen) = withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            val cropped = croppedBitmap(wallpaper, wallpaperApplySize(context))
            bitmap = cropped
            this@WallpaperUpdater.logD {
                "Cropped output ${cropped.width} × ${cropped.height}. wallpaperId: ${wallpaper.id}"
            }
            manager.setBitmap(cropped, null, true, target.toFlag())
        } catch (e: Throwable) {
            this@WallpaperUpdater.logW {
                "Custom crop failed, falling back to stream. wallpaperId: ${wallpaper.id}, error: ${e.message}"
            }
            applyStream(wallpaper, target)
        } finally {
            bitmap?.recycle()
        }
    }

    private suspend fun croppedBitmap(wallpaper: Wallpaper, target: Size): Bitmap {
        val uri = wallpaper.uri.toUri()
        val targetAspect = target.width.toFloat() / target.height

        // Probe first so the decode can be downsampled without ever allocating the full source.
        val sourceBounds = readWallpaperBounds(context, uri)
        val decode = sourceBounds?.let { decodeSize(it, target, wallpaper.cropZoom) }
            ?: Size(target.width, target.height)

        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(decode.width, decode.height)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
        val source = SingletonImageLoader.get(context).execute(request).image?.toBitmap()
            ?: error("Cannot decode wallpaper. uri: ${wallpaper.uri}")

        try {
            val crop = viewport(
                sourceWidth = source.width,
                sourceHeight = source.height,
                focalX = wallpaper.cropFocalX,
                focalY = wallpaper.cropFocalY,
                zoom = wallpaper.cropZoom,
                targetAspect = targetAspect,
            )
            check(!crop.isEmpty) { "Empty crop for wallpaper. uri: ${wallpaper.uri}" }

            return Bitmap.createBitmap(target.width, target.height, Bitmap.Config.ARGB_8888).also { out ->
                Canvas(out).drawBitmap(
                    source,
                    crop,
                    Rect(0, 0, target.width, target.height),
                    Paint(Paint.FILTER_BITMAP_FLAG),
                )
            }
        } finally {
            source.recycle()
        }
    }

    private fun decodeSize(source: Size, target: Size, zoom: Float): Size {
        // Coil decodes the EXIF-rotated image, so the probed dimensions may be transposed. Covering
        // both orientations is cheaper than parsing EXIF and never requests less than the real one.
        val upright = neededDecodeSize(source, target, zoom)
        val transposed = neededDecodeSize(Size(source.height, source.width), target, zoom)
        val width = max(upright.width, transposed.width).toFloat()
        val height = max(upright.height, transposed.height).toFloat()

        // Budget guard only: at high zoom the crop region holds fewer pixels than the screen shows,
        // so it still upscales, but a pathological zoom cannot balloon the allocation.
        val maxPixels = decodeBudgetBytes() / BYTES_PER_PIXEL
        val bound = min(
            MAX_DECODE_DIMENSION / max(width, height),
            sqrt(maxPixels / (width * height)),
        )
        return if (bound < 1f) {
            Size((width * bound).roundToInt().coerceAtLeast(1), (height * bound).roundToInt().coerceAtLeast(1))
        } else {
            Size(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1))
        }
    }

    /** Enough pixels for the crop region to land on the target at 1:1, capped at the source. */
    private fun neededDecodeSize(source: Size, target: Size, zoom: Float): Size {
        val safeZoom = zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
        val targetAspect = target.width / target.height.toFloat()
        val sourceAspect = source.width / source.height.toFloat()

        val width = min(source.width.toFloat(), target.width * safeZoom * max(1f, sourceAspect / targetAspect))
        val height = min(source.height.toFloat(), target.height * safeZoom * max(1f, targetAspect / sourceAspect))
        return Size(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1))
    }

    private fun TargetScreen.toFlag() = when (this) {
        TargetScreen.Both -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        TargetScreen.Home -> WallpaperManager.FLAG_SYSTEM
        TargetScreen.Lock -> WallpaperManager.FLAG_LOCK
    }

}

/**
 * The cropped apply target: the current window bounds, including the system bars.
 *
 * Cropped output is screen-sized by design, so the editor's frame and the applied wallpaper share
 * one aspect. A launcher that pans a wider virtual desktop may shift the framing — accepted.
 */
internal fun wallpaperApplySize(context: Context): Size {
    val bounds = context.getSystemService(WindowManager::class.java).currentWindowMetrics.bounds
    return Size(bounds.width(), bounds.height())
}

/** Stored pixel bounds of a wallpaper, or null when the file cannot be read. */
internal fun readWallpaperBounds(context: Context, uri: Uri): Size? = runCatching {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    val width = options.outWidth
    val height = options.outHeight
    if (width > 0 && height > 0) Size(width, height) else null
}.getOrNull()

/**
 * A quarter of the heap for the decoded bitmap: Coil holds a second copy while applying EXIF, and
 * the output bitmap is allocated alongside it. The 8 MP floor keeps a 12 MP photo 1:1 at 1x.
 */
private fun decodeBudgetBytes(): Long =
    (Runtime.getRuntime().maxMemory() / 4).coerceIn(MIN_DECODE_BUDGET_BYTES, MAX_DECODE_BUDGET_BYTES)

private const val MIN_DECODE_BUDGET_BYTES = 32L * 1024 * 1024
private const val MAX_DECODE_BUDGET_BYTES = 64L * 1024 * 1024
private const val BYTES_PER_PIXEL = 4f
private const val MAX_DECODE_DIMENSION = 4096f

