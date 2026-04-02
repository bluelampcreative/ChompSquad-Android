package com.bluelampcreative.chompsquad.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton

private const val MAX_DIMENSION = 2048
private const val TARGET_QUALITY = 78
private const val MIN_QUALITY = 10
private const val MAX_FILE_BYTES = 2 * 1024 * 1024L // 2 MB

interface ImagePreprocessor {
  /**
   * Resizes [uri] so its longest edge ≤ 2 048 px, then compresses to JPEG at quality 78. If the
   * result exceeds 2 MB a binary search over quality [10, 77] finds the highest quality that still
   * fits. All work runs on [Dispatchers.Default].
   */
  suspend fun process(uri: Uri): Result<ByteArray>
}

@Singleton(binds = [ImagePreprocessor::class])
class DefaultImagePreprocessor(private val context: Context) : ImagePreprocessor {

  override suspend fun process(uri: Uri): Result<ByteArray> =
      withContext(Dispatchers.Default) { runCatching { compress(scale(decode(uri))) } }

  private fun decode(uri: Uri): Bitmap {
    return context.contentResolver.openInputStream(uri).use { stream ->
      checkNotNull(stream) { "Cannot open stream for $uri" }
      BitmapFactory.decodeStream(stream) ?: error("Failed to decode bitmap from $uri")
    }
  }

  private fun scale(bitmap: Bitmap): Bitmap {
    val longest = maxOf(bitmap.width, bitmap.height)
    if (longest <= MAX_DIMENSION) return bitmap
    val factor = MAX_DIMENSION.toFloat() / longest
    val w = (bitmap.width * factor).toInt()
    val h = (bitmap.height * factor).toInt()
    return Bitmap.createScaledBitmap(bitmap, w, h, /* filter= */ true)
  }

  private fun compress(bitmap: Bitmap): ByteArray {
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, TARGET_QUALITY, out)
    if (out.size() <= MAX_FILE_BYTES) return out.toByteArray()

    // Binary search: highest quality in [MIN_QUALITY, TARGET_QUALITY - 1] that fits in 2 MB.
    var lo = MIN_QUALITY
    var hi = TARGET_QUALITY - 1
    var result: ByteArray? = null
    while (lo <= hi) {
      val mid = (lo + hi) / 2
      out.reset()
      bitmap.compress(Bitmap.CompressFormat.JPEG, mid, out)
      if (out.size() <= MAX_FILE_BYTES) {
        result = out.toByteArray()
        lo = mid + 1
      } else {
        hi = mid - 1
      }
    }
    return result
        ?: run {
          out.reset()
          bitmap.compress(Bitmap.CompressFormat.JPEG, MIN_QUALITY, out)
          out.toByteArray()
        }
  }
}
