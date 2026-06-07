package com.govautofill.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

object ImageProcessor {

    enum class ImageType(val targetW: Int, val targetH: Int, val fileName: String) {
        PHOTO(300, 300, "photo.jpg"),
        SIGNATURE(300, 80, "signature.jpg")
    }

    /**
     * Full pipeline: load → fix EXIF rotation → resize → whiten → compress → save
     * Returns the output File and its size in KB
     */
    fun process(
        context: Context,
        uri: Uri,
        type: ImageType,
        whitenLevel: Int = 0   // 0–100
    ): ProcessResult {
        // 1. Decode bitmap from URI
        val raw = context.contentResolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it)
        } ?: throw IllegalArgumentException("Cannot decode image")

        // 2. Fix EXIF rotation
        val rotated = fixExifRotation(context, uri, raw)

        // 3. Resize to target dimensions (center-crop for photo, stretch-fit for signature)
        val resized = if (type == ImageType.PHOTO) {
            centerCrop(rotated, type.targetW, type.targetH)
        } else {
            fitResize(rotated, type.targetW, type.targetH)
        }

        // 4. Apply background whitener
        val whitened = if (whitenLevel > 0) whiteBackground(resized, whitenLevel) else resized

        // 5. Save to internal storage
        val dir = File(context.filesDir, "media_vault").also { it.mkdirs() }
        val outFile = File(dir, type.fileName)
        FileOutputStream(outFile).use { out ->
            whitened.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        return ProcessResult(
            file = outFile,
            bitmap = whitened,
            sizeKb = (outFile.length() / 1024).toInt(),
            width = whitened.width,
            height = whitened.height
        )
    }

    // ── EXIF rotation fix ────────────────────────────────────────────────────
    private fun fixExifRotation(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
        val rotation = try {
            context.contentResolver.openInputStream(uri)!!.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> -1f  // flip
                    else -> 0f
                }
            }
        } catch (e: Exception) { 0f }

        if (rotation == 0f) return bmp
        val matrix = Matrix()
        if (rotation == -1f) matrix.preScale(-1f, 1f) else matrix.postRotate(rotation)
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    // ── Center-crop to exact WxH ─────────────────────────────────────────────
    private fun centerCrop(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcRatio = src.width.toFloat() / src.height
        val dstRatio = targetW.toFloat() / targetH

        val (scaledW, scaledH) = if (srcRatio > dstRatio) {
            (src.height * dstRatio).toInt() to src.height
        } else {
            src.width to (src.width / dstRatio).toInt()
        }

        val x = (src.width - scaledW) / 2
        val y = (src.height - scaledH) / 2
        val cropped = Bitmap.createBitmap(src, x, y, scaledW, scaledH)
        return Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
    }

    // ── Fit-resize (no crop, may distort for signature) ──────────────────────
    private fun fitResize(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val result = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        val scale = minOf(targetW.toFloat() / src.width, targetH.toFloat() / src.height)
        val scaledW = (src.width * scale).toInt()
        val scaledH = (src.height * scale).toInt()
        val left = (targetW - scaledW) / 2f
        val top = (targetH - scaledH) / 2f
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(src, scaledW, scaledH, true),
            left, top, paint
        )
        return result
    }

    // ── Background whitener ───────────────────────────────────────────────────
    // Brightens near-white pixels toward pure white based on level (0-100)
    fun whiteBackground(src: Bitmap, level: Int): Bitmap {
        val threshold = 255 - (level * 1.2f).toInt().coerceIn(0, 200)
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        for (i in pixels.indices) {
            val r = Color.red(pixels[i])
            val g = Color.green(pixels[i])
            val b = Color.blue(pixels[i])
            val brightness = (r + g + b) / 3
            if (brightness >= threshold) {
                // Blend toward white proportionally
                val blend = ((brightness - threshold).toFloat() / (255 - threshold)).coerceIn(0f, 1f)
                val nr = (r + (255 - r) * blend).toInt().coerceIn(0, 255)
                val ng = (g + (255 - g) * blend).toInt().coerceIn(0, 255)
                val nb = (b + (255 - b) * blend).toInt().coerceIn(0, 255)
                pixels[i] = Color.rgb(nr, ng, nb)
            }
        }
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    data class ProcessResult(
        val file: File,
        val bitmap: Bitmap,
        val sizeKb: Int,
        val width: Int,
        val height: Int
    )
}
