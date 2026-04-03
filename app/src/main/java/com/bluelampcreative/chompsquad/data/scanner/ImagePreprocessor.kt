package com.bluelampcreative.chompsquad.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton

private const val MAX_DIMENSION = 2048
private const val TARGET_QUALITY = 78
private const val MIN_QUALITY = 10
private const val MAX_FILE_BYTES = 2L * 1024 * 1024 // 2 MB
private const val ROTATION_90 = 90f
private const val ROTATION_180 = 180f
private const val ROTATION_270 = 270f

interface ImagePreprocessor {
  /**
   * Preprocesses [uri] for upload: reads EXIF orientation, scales the longest edge to ≤ 2 048 px,
   * and compresses to JPEG at quality 78. If the result exceeds 2 MB a binary search over quality
   * [10, 77] finds the highest quality that still fits. Returns [Result.failure] if the 2 MB cap
   * cannot be met. I/O runs on [Dispatchers.IO]; CPU work on [Dispatchers.Default].
   */
  suspend fun process(uri: Uri): Result<ByteArray>
}

@Singleton(binds = [ImagePreprocessor::class])
class DefaultImagePreprocessor(private val context: Context) : ImagePreprocessor {

  @Suppress("TooGenericExceptionCaught") // wraps all failure modes into Result.failure
  override suspend fun process(uri: Uri): Result<ByteArray> {
    return try {
      val raw = withContext(Dispatchers.IO) { readBytes(uri) }
      val bytes =
          withContext(Dispatchers.Default) {
            val bitmap = decodeWithSample(raw)
            val oriented = applyExifRotation(bitmap, raw)
            compress(scale(oriented))
          }
      Result.success(bytes)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Reads all bytes from [uri] via [android.content.ContentResolver]. Blocking — call on IO. */
  private fun readBytes(uri: Uri): ByteArray {
    return context.contentResolver.openInputStream(uri).use { stream ->
      checkNotNull(stream) { "Cannot open stream for $uri" }
      stream.readBytes()
    }
  }

  /**
   * Decodes [raw] with an [BitmapFactory.Options.inSampleSize] computed from the bounds so the
   * decoded bitmap is already near [MAX_DIMENSION], avoiding OOM on large camera/gallery images.
   */
  private fun decodeWithSample(raw: ByteArray): Bitmap {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(raw, 0, raw.size, opts)
    val longest = maxOf(opts.outWidth, opts.outHeight)
    opts.inSampleSize =
        if (longest > MAX_DIMENSION) (longest / MAX_DIMENSION).coerceAtLeast(1) else 1
    opts.inJustDecodeBounds = false
    return BitmapFactory.decodeByteArray(raw, 0, raw.size, opts) ?: error("Failed to decode bitmap")
  }

  /** Rotates [bitmap] to match the EXIF orientation embedded in [raw]. */
  private fun applyExifRotation(bitmap: Bitmap, raw: ByteArray): Bitmap {
    val exif = ExifInterface(ByteArrayInputStream(raw))
    val degrees =
        when (
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
          ExifInterface.ORIENTATION_ROTATE_90 -> ROTATION_90
          ExifInterface.ORIENTATION_ROTATE_180 -> ROTATION_180
          ExifInterface.ORIENTATION_ROTATE_270 -> ROTATION_270
          else -> return bitmap
        }
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }

  private fun scale(bitmap: Bitmap): Bitmap {
    val longest = maxOf(bitmap.width, bitmap.height)
    if (longest <= MAX_DIMENSION) return bitmap
    val factor = MAX_DIMENSION.toFloat() / longest
    // coerceAtLeast(1) guards against 0 px on extreme aspect ratios (e.g. 1×5000).
    val w = (bitmap.width * factor).toInt().coerceAtLeast(1)
    val h = (bitmap.height * factor).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, w, h, /* filter= */ true)
  }

  private fun compress(bitmap: Bitmap): ByteArray {
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, TARGET_QUALITY, out)
    if (out.size().toLong() <= MAX_FILE_BYTES) return out.toByteArray()

    // Binary search: highest quality in [MIN_QUALITY, TARGET_QUALITY - 1] that fits in 2 MB.
    var lo = MIN_QUALITY
    var hi = TARGET_QUALITY - 1
    var result: ByteArray? = null
    while (lo <= hi) {
      val mid = (lo + hi) / 2
      out.reset()
      bitmap.compress(Bitmap.CompressFormat.JPEG, mid, out)
      if (out.size().toLong() <= MAX_FILE_BYTES) {
        result = out.toByteArray()
        lo = mid + 1
      } else {
        hi = mid - 1
      }
    }
    // Return the best fitting result, or fail — never return an oversized payload.
    return result
        ?: error(
            "Image (${bitmap.width}×${bitmap.height}) cannot be compressed to ≤ 2 MB at quality $MIN_QUALITY"
        )
  }
}
