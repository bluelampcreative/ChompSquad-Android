package com.bluelampcreative.chompsquad.feature.camera

import android.net.Uri
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CameraViewModel :
    CoreViewModel<CameraViewState, CameraAction, CameraUiEvent>(CameraViewState()) {

  override fun reducer(state: CameraViewState, action: CameraAction): CameraViewState =
      when (action) {
        is CameraAction.PermissionUpdated ->
            state.copy(
                hasCameraPermission = action.granted,
                permissionPermanentlyDenied = action.permanent,
            )
        is CameraAction.ImageCaptured ->
            state.copy(
                capturedImages = (state.capturedImages + action.uri).take(MAX_SCAN_IMAGES),
                isCapturing = false,
            )
        is CameraAction.ImagesSelected ->
            state.copy(
                capturedImages = (state.capturedImages + action.uris).take(MAX_SCAN_IMAGES),
            )
        is CameraAction.CaptureStarted -> state.copy(isCapturing = true)
        is CameraAction.CaptureFailed -> state.copy(isCapturing = false)
        is CameraAction.ImageRemoved ->
            if (action.index in state.capturedImages.indices)
                state.copy(
                    capturedImages =
                        state.capturedImages.toMutableList().also { it.removeAt(action.index) }
                )
            else state
        is CameraAction.FlashToggled ->
            state.copy(
                flashMode =
                    when (state.flashMode) {
                      FlashMode.Off -> FlashMode.On
                      FlashMode.On -> FlashMode.Auto
                      FlashMode.Auto -> FlashMode.Off
                    }
            )
        is CameraAction.CameraFlipped -> state.copy(isFrontCamera = !state.isFrontCamera)
      }

  override fun handleEvent(event: CameraUiEvent) {
    when (event) {
      is CameraUiEvent.OnPermissionResult ->
          state.dispatch(CameraAction.PermissionUpdated(event.granted, event.permanentlyDenied))
      is CameraUiEvent.OnImagesSelected -> state.dispatch(CameraAction.ImagesSelected(event.uris))
      is CameraUiEvent.OnImageRemoved -> state.dispatch(CameraAction.ImageRemoved(event.index))
      CameraUiEvent.OnFlipCamera -> state.dispatch(CameraAction.CameraFlipped)
      CameraUiEvent.OnToggleFlash -> state.dispatch(CameraAction.FlashToggled)
      // TODO(task 2.3): store images in ScanSessionRepository and navigate to ScanSubmission
      CameraUiEvent.OnNext -> navigate(NavEvent.GoBack)
      CameraUiEvent.OnClose -> navigate(NavEvent.GoBack)
    }
  }

  /** Called from the composable when the shutter is pressed and a capture is beginning. */
  fun onCaptureStarted() = state.dispatch(CameraAction.CaptureStarted)

  /** Called from the composable when CameraX successfully saves an image to disk. */
  fun onImageCaptured(uri: Uri) = state.dispatch(CameraAction.ImageCaptured(uri))

  /** Called from the composable when CameraX image capture fails. */
  fun onCaptureFailed() = state.dispatch(CameraAction.CaptureFailed)
}
