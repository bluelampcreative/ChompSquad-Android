package com.bluelampcreative.chompsquad.data.scanner

import android.net.Uri
import com.bluelampcreative.chompsquad.domain.model.Recipe
import org.koin.core.annotation.Singleton

/**
 * In-memory store that bridges the Camera capture screen (which sets the images to scan) and the
 * Scan Submission screen (which sets the resulting [Recipe] after the upload succeeds).
 *
 * Scoped as a singleton so both screens share the same instance without persisting anything to
 * disk. Call [clear] when the scan flow is fully complete.
 */
interface ScanSessionRepository {
  fun setPendingImages(uris: List<Uri>)

  fun getPendingImages(): List<Uri>

  fun setScanResult(recipe: Recipe)

  fun getScanResult(): Recipe?

  fun clear()
}

@Singleton(binds = [ScanSessionRepository::class])
class DefaultScanSessionRepository : ScanSessionRepository {
  @Volatile private var pendingImages: List<Uri> = emptyList()
  @Volatile private var scanResult: Recipe? = null

  override fun setPendingImages(uris: List<Uri>) {
    pendingImages = uris.toList()
  }

  override fun getPendingImages(): List<Uri> = pendingImages.toList()

  override fun setScanResult(recipe: Recipe) {
    scanResult = recipe
  }

  override fun getScanResult(): Recipe? = scanResult

  override fun clear() {
    pendingImages = emptyList()
    scanResult = null
  }
}
