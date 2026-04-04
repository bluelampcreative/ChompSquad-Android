package com.bluelampcreative.chompsquad.feature.camera

import android.net.Uri
import com.bluelampcreative.chompsquad.core.ViewAction

/** Maximum number of pages (images) allowed per scan. */
const val MAX_SCAN_IMAGES = 5

enum class FlashMode {
  Off,
  On,
  Auto,
}

data class CameraViewState(
    val capturedImages: List<Uri> = emptyList(),
    val isCapturing: Boolean = false,
    val flashMode: FlashMode = FlashMode.Off,
    val isFrontCamera: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val permissionPermanentlyDenied: Boolean = false,
    /**
     * Remaining scans for this billing period. `null` means not yet loaded or the user has
     * unlimited scans (pro entitlement). `0` means the monthly cap is exhausted.
     */
    val scansRemaining: Int? = null,
) {
  /** True when the server-reported scan cap has been reached and the user must upgrade. */
  val isScanCapReached: Boolean
    get() = scansRemaining == 0
}

sealed interface CameraAction : ViewAction {
  data class PermissionUpdated(val granted: Boolean, val permanent: Boolean) : CameraAction

  data class ImageCaptured(val uri: Uri) : CameraAction

  data class ImagesSelected(val uris: List<Uri>) : CameraAction

  data object CaptureStarted : CameraAction

  data object CaptureFailed : CameraAction

  data class ImageRemoved(val index: Int) : CameraAction

  data object FlashToggled : CameraAction

  data object CameraFlipped : CameraAction

  /** Loaded from the server profile; only dispatched for non-pro users. */
  data class ScanCountLoaded(val remaining: Int) : CameraAction
}

sealed interface CameraUiEvent {
  data object OnFlipCamera : CameraUiEvent

  data object OnToggleFlash : CameraUiEvent

  data class OnImagesSelected(val uris: List<Uri>) : CameraUiEvent

  data class OnImageRemoved(val index: Int) : CameraUiEvent

  data class OnPermissionResult(val granted: Boolean, val permanentlyDenied: Boolean) :
      CameraUiEvent

  data object OnNext : CameraUiEvent

  data object OnClose : CameraUiEvent

  /** User tapped the upgrade CTA on the scan-cap screen. */
  data object OnUpgrade : CameraUiEvent
}
