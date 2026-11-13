package com.axondragonscale.wallmatic.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/**
 * The Android half of a wallpaper change: decode, crop and hand off to the system. Selection and
 * persistence live in [WallpaperCycler].
 */
class WallpaperApplier(private val context: Context) {

    private val manager = WallpaperManager.getInstance(context)

    /**
     * Applies [wallpaper] to [target]; failures are returned, never thrown.
     *
     * A wallpaper without a custom crop is streamed straight to the system, which does its own
     * crop. A custom crop is decoded, cropped with [viewport] and applied as a bitmap; if any step
     * of that fails, the stream path is used instead so the wallpaper is still set.
     */
    suspend fun apply(wallpaper: Wallpaper, target: TargetScreen): Result<Unit> = try {
        if (wallpaper.hasCustomCrop) applyCropped(wallpaper, target) else applyStream(wallpaper, target)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        this.logE { "Setting wallpaper failed. wallpaperId: ${wallpaper.id}, target: $target, error: ${e.message}" }
        Result.failure(e)
    }

    /**
     * The cropped apply target: the current window bounds, including the system bars.
     *
     * Cropped output is screen-sized by design, so the editor's frame and the applied wallpaper
     * share one aspect. A launcher that pans a wider virtual desktop may shift the framing.
     */
    fun applySize(): Size {
        val bounds = context.getSystemService(WindowManager::class.java).currentWindowMetrics.bounds
        return Size(bounds.width(), bounds.height())
    }

    /** Stored pixel bounds of [uri], or null when the file cannot be read. */
    fun sourceSize(uri: String): Size? = runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri.toUri())?.use { BitmapFactory.decodeStream(it, null, options) }
        val width = options.outWidth
        val height = options.outHeight
        if (width > 0 && height > 0) Size(width, height) else null
    }.getOrNull()

    private fun applyStream(wallpaper: Wallpaper, target: TargetScreen): Unit {
        val stream = context.contentResolver.openInputStream(wallpaper.uri.toUri())
            ?: error("Cannot open wallpaper. uri: ${wallpaper.uri}")
        stream.use { manager.setStream(it, null, true, target.toFlag()) }
    }

    private suspend fun applyCropped(wallpaper: Wallpaper, target: TargetScreen): Unit = withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            val cropped = croppedBitmap(wallpaper, applySize())
            bitmap = cropped
            this@WallpaperApplier.logD {
                "Cropped output ${cropped.width} × ${cropped.height}. wallpaperId: ${wallpaper.id}"
            }
            manager.setBitmap(cropped, null, true, target.toFlag())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            this@WallpaperApplier.logW {
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
        val sourceBounds = sourceSize(wallpaper.uri)
        val decode = sourceBounds?.let { decodeSize(it, target, wallpaper.crop.zoom) }
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
                crop = wallpaper.crop,
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
 * A quarter of the heap for the decoded bitmap: Coil holds a second copy while applying EXIF, and
 * the output bitmap is allocated alongside it. The 8 MP floor keeps a 12 MP photo 1:1 at 1x.
 */
private fun decodeBudgetBytes(): Long =
    (Runtime.getRuntime().maxMemory() / 4).coerceIn(MIN_DECODE_BUDGET_BYTES, MAX_DECODE_BUDGET_BYTES)

private const val MIN_DECODE_BUDGET_BYTES = 32L * 1024 * 1024
private const val MAX_DECODE_BUDGET_BYTES = 64L * 1024 * 1024
private const val BYTES_PER_PIXEL = 4f
private const val MAX_DECODE_DIMENSION = 4096f
