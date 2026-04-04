package com.bluelampcreative.chompsquad.feature.camera

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.purchases.SubscriptionRepository
import com.bluelampcreative.chompsquad.data.remote.UserProfileApi
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CameraViewModel(
    private val scanSessionRepository: ScanSessionRepository,
    private val userProfileApi: UserProfileApi,
    private val subscriptionRepository: SubscriptionRepository,
) : CoreViewModel<CameraViewState, CameraAction, CameraUiEvent>(CameraViewState()) {

  init {
    // Load remaining scan count for non-pro users so the UI can show the indicator and gate
    // the scan flow before hitting the server. Pro users have unlimited scans — skip the call.
    viewModelScope.launch {
      if (!subscriptionRepository.entitlementStatus.value.hasPro) {
        userProfileApi.getProfile().onSuccess { profile ->
          // scansRemaining == null means the server grants unlimited (e.g. beta users).
          // Only dispatch when the server gives a concrete count.
          profile.scansRemaining?.let { remaining ->
            state.dispatch(CameraAction.ScanCountLoaded(remaining))
          }
        }
        // On failure: don't block the user. The server will return 402/403 if cap is exceeded.
      }
    }
  }

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
        is CameraAction.ImageRemoved -> state.removeImageAt(action.index)
        is CameraAction.FlashToggled -> state.copy(flashMode = state.flashMode.next())
        is CameraAction.CameraFlipped -> state.copy(isFrontCamera = !state.isFrontCamera)
        is CameraAction.ScanCountLoaded -> state.copy(scansRemaining = action.remaining)
      }

  override fun handleEvent(event: CameraUiEvent) {
    when (event) {
      is CameraUiEvent.OnPermissionResult ->
          state.dispatch(CameraAction.PermissionUpdated(event.granted, event.permanentlyDenied))
      is CameraUiEvent.OnImagesSelected -> state.dispatch(CameraAction.ImagesSelected(event.uris))
      is CameraUiEvent.OnImageRemoved -> state.dispatch(CameraAction.ImageRemoved(event.index))
      is CameraUiEvent.OnFlipCamera -> state.dispatch(CameraAction.CameraFlipped)
      is CameraUiEvent.OnToggleFlash -> state.dispatch(CameraAction.FlashToggled)
      is CameraUiEvent.OnNext -> {
        if (state.value.isScanCapReached) {
          navigate(NavEvent.NavigateToPaywall)
        } else {
          scanSessionRepository.setPendingImages(state.value.capturedImages.toList())
          navigate(NavEvent.NavigateToScanSubmission)
        }
      }
      is CameraUiEvent.OnUpgrade -> navigate(NavEvent.NavigateToPaywall)
      is CameraUiEvent.OnClose -> navigate(NavEvent.GoBack)
    }
  }

  /** Called from the composable when the shutter is pressed and a capture is beginning. */
  fun onCaptureStarted() = state.dispatch(CameraAction.CaptureStarted)

  /** Called from the composable when CameraX successfully saves an image to disk. */
  fun onImageCaptured(uri: Uri) = state.dispatch(CameraAction.ImageCaptured(uri))

  /** Called from the composable when CameraX image capture fails. */
  fun onCaptureFailed() = state.dispatch(CameraAction.CaptureFailed)
}

private fun CameraViewState.removeImageAt(index: Int): CameraViewState =
    if (index in capturedImages.indices)
        copy(capturedImages = capturedImages.toMutableList().also { it.removeAt(index) })
    else this

private fun FlashMode.next(): FlashMode =
    when (this) {
      FlashMode.Off -> FlashMode.On
      FlashMode.On -> FlashMode.Auto
      FlashMode.Auto -> FlashMode.Off
    }
