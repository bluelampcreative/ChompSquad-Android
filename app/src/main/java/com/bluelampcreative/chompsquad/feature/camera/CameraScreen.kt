package com.bluelampcreative.chompsquad.feature.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.bluelampcreative.chompsquad.feature.signin.findActivity
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.brandGolden
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.compose.koinViewModel

@Composable
fun CameraScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  // Runtime CAMERA permission
  val permissionLauncher =
      rememberLauncherForActivityResult(RequestPermission()) { granted ->
        // shouldShowRequestPermissionRationale requires an Activity; fall back to false (treat
        // as non-permanent) if the context can't be unwrapped — e.g. in Compose Preview hosts.
        val activity = context.findActivity()
        val permanent =
            !granted &&
                (activity == null ||
                    !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
        viewModel.handleEvent(CameraUiEvent.OnPermissionResult(granted, permanent))
      }
  LaunchedEffect(Unit) {
    val alreadyGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    if (alreadyGranted) {
      viewModel.handleEvent(
          CameraUiEvent.OnPermissionResult(granted = true, permanentlyDenied = false)
      )
    } else {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  // Resolve the permission action for the rationale screen before entering composition.
  val permissionAction: () -> Unit =
      if (viewState.permissionPermanentlyDenied) {
        {
          runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                  data = Uri.fromParts("package", context.packageName, null)
                }
            )
          }
        }
      } else {
        { permissionLauncher.launch(Manifest.permission.CAMERA) }
      }

  CameraScreenContent(
      viewState = viewState,
      onHandleEvent = viewModel::handleEvent,
      onCaptureStart = viewModel::onCaptureStarted,
      onImageCapture = viewModel::onImageCaptured,
      onCaptureFail = viewModel::onCaptureFailed,
      onPermissionAction = permissionAction,
      modifier = modifier,
  )
}

private const val SCANS_REMAINING_WARN_THRESHOLD = 3

@Composable
private fun CameraScreenContent(
    viewState: CameraViewState,
    onHandleEvent: (CameraUiEvent) -> Unit,
    onCaptureStart: () -> Unit,
    onImageCapture: (Uri) -> Unit,
    onCaptureFail: () -> Unit,
    onPermissionAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    when {
      !viewState.hasCameraPermission ->
          PermissionContent(
              permanentlyDenied = viewState.permissionPermanentlyDenied,
              onClose = { onHandleEvent(CameraUiEvent.OnClose) },
              onAction = onPermissionAction,
          )
      viewState.isScanCapReached ->
          ScanCapContent(
              onUpgrade = { onHandleEvent(CameraUiEvent.OnUpgrade) },
              onClose = { onHandleEvent(CameraUiEvent.OnClose) },
          )
      else ->
          LiveCameraContent(
              viewState = viewState,
              onHandleEvent = onHandleEvent,
              onCaptureStart = onCaptureStart,
              onImageCapture = onImageCapture,
              onCaptureFail = onCaptureFail,
          )
    }
  }
}

// The broad catch is intentional: any CameraX binding failure (unavailable hardware, security
// restriction, etc.) should fail silently rather than crash — the preview stays black and the
// user can close the screen.
@Suppress("TooGenericExceptionCaught")
@Composable
private fun LiveCameraContent(
    viewState: CameraViewState,
    onHandleEvent: (CameraUiEvent) -> Unit,
    onCaptureStart: () -> Unit,
    onImageCapture: (Uri) -> Unit,
    onCaptureFail: () -> Unit,
) {
  val context = LocalContext.current

  val previewView = remember { PreviewView(context) }
  val imageCapture = remember { ImageCapture.Builder().build() }
  val lifecycleOwner = LocalLifecycleOwner.current

  // Bind/rebind camera when lens facing changes
  LaunchedEffect(viewState.isFrontCamera) {
    try {
      val provider = suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
              try {
                if (cont.isActive) cont.resume(future.get())
              } catch (e: Exception) {
                if (cont.isActive) cont.resumeWithException(e)
              }
            },
            ContextCompat.getMainExecutor(context),
        )
        cont.invokeOnCancellation { future.cancel(false) }
      }
      provider.unbindAll()
      val selector =
          if (viewState.isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
          else CameraSelector.DEFAULT_BACK_CAMERA
      val preview =
          Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
      provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      // Camera unavailable on this device/configuration — preview stays black
    }
  }

  // Keep flash mode in sync with ImageCapture
  LaunchedEffect(viewState.flashMode) {
    imageCapture.flashMode = viewState.flashMode.toCameraXFlashMode()
  }

  // Photo picker — limited to remaining capacity so the system picker shows the correct max.
  // key() forces re-registration when remaining slots change (safe: picker is always closed
  // before the count updates).
  val remaining = MAX_SCAN_IMAGES - viewState.capturedImages.size
  val photoPickerLauncher =
      key(remaining) {
        rememberLauncherForActivityResult(
            PickMultipleVisualMedia(maxItems = maxOf(1, remaining))
        ) { uris ->
          if (uris.isNotEmpty()) onHandleEvent(CameraUiEvent.OnImagesSelected(uris))
        }
      }

  val canCapture = viewState.capturedImages.size < MAX_SCAN_IMAGES && !viewState.isCapturing
  // Immediate in-composable lock prevents concurrent captures from fast double-taps before the
  // reducer's isCapturing flag propagates back through recomposition.
  var captureLock by remember { mutableStateOf(false) }
  val onCapture = {
    if (canCapture && !captureLock) {
      captureLock = true
      onCaptureStart()
      val outputDir = File(context.cacheDir, "scan_images").also { it.mkdirs() }
      val file = File(outputDir, "scan_${System.currentTimeMillis()}.jpg")
      imageCapture.takePicture(
          ImageCapture.OutputFileOptions.Builder(file).build(),
          ContextCompat.getMainExecutor(context),
          object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
              captureLock = false
              onImageCapture(output.savedUri ?: Uri.fromFile(file))
            }

            override fun onError(exception: ImageCaptureException) {
              captureLock = false
              onCaptureFail()
            }
          },
      )
    }
  }

  Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    CameraTopBar(
        viewState = viewState,
        onClose = { onHandleEvent(CameraUiEvent.OnClose) },
        onToggleFlash = { onHandleEvent(CameraUiEvent.OnToggleFlash) },
        modifier = Modifier.align(Alignment.TopStart),
    )

    CameraBottomBar(
        viewState = viewState,
        canCapture = canCapture,
        onCapture = onCapture,
        onGalleryPick = {
          photoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        },
        onHandleEvent = onHandleEvent,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

@Composable
private fun CameraTopBar(
    viewState: CameraViewState,
    onClose: () -> Unit,
    onToggleFlash: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .background(Color.Black.copy(alpha = 0.45f))
              .statusBarsPadding()
              .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(onClick = onClose) {
      Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
    }
    Spacer(Modifier.weight(1f))
    if (viewState.capturedImages.isNotEmpty()) {
      Text(
          text = "${viewState.capturedImages.size}/$MAX_SCAN_IMAGES",
          style =
              MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
              ),
          modifier = Modifier.padding(horizontal = 8.dp),
      )
    }
    val scansRemaining = viewState.scansRemaining
    if (scansRemaining != null && scansRemaining > 0) {
      val warn = scansRemaining <= SCANS_REMAINING_WARN_THRESHOLD
      Text(
          text = if (warn) "⚠ $scansRemaining scans left" else "$scansRemaining scans left",
          style =
              MaterialTheme.typography.labelMedium.copy(
                  color = if (warn) brandGolden else Color.White,
              ),
          modifier = Modifier.padding(horizontal = 8.dp),
      )
    }
    IconButton(onClick = onToggleFlash) {
      Icon(
          imageVector =
              when (viewState.flashMode) {
                FlashMode.Off -> Icons.Default.FlashOff
                FlashMode.On -> Icons.Default.FlashOn
                FlashMode.Auto -> Icons.Default.FlashAuto
              },
          contentDescription = "Flash: ${viewState.flashMode.name}",
          tint = Color.White,
      )
    }
  }
}

private fun FlashMode.toCameraXFlashMode() =
    when (this) {
      FlashMode.Off -> ImageCapture.FLASH_MODE_OFF
      FlashMode.On -> ImageCapture.FLASH_MODE_ON
      FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
    }

@Composable
private fun CameraBottomBar(
    viewState: CameraViewState,
    canCapture: Boolean,
    onCapture: () -> Unit,
    onGalleryPick: () -> Unit,
    onHandleEvent: (CameraUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)),
  ) {
    if (viewState.capturedImages.isNotEmpty()) {
      ThumbnailStrip(
          images = viewState.capturedImages,
          onRemove = { index -> onHandleEvent(CameraUiEvent.OnImageRemoved(index)) },
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 40.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
          onClick = onGalleryPick,
          enabled = viewState.capturedImages.size < MAX_SCAN_IMAGES && !viewState.isCapturing,
      ) {
        Icon(
            Icons.Default.PhotoLibrary,
            contentDescription = "Choose from gallery",
            tint = Color.White,
            modifier = Modifier.size(32.dp),
        )
      }
      ShutterButton(isCapturing = viewState.isCapturing, enabled = canCapture, onClick = onCapture)
      if (viewState.capturedImages.isNotEmpty()) {
        FilledIconButton(
            onClick = { onHandleEvent(CameraUiEvent.OnNext) },
            modifier = Modifier.size(56.dp),
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
        ) {
          Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Next",
              tint = Color.White,
          )
        }
      } else {
        IconButton(
            onClick = { onHandleEvent(CameraUiEvent.OnFlipCamera) },
            enabled = !viewState.isCapturing,
        ) {
          Icon(
              Icons.Default.FlipCameraAndroid,
              contentDescription = "Flip camera",
              tint = Color.White,
              modifier = Modifier.size(32.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ShutterButton(isCapturing: Boolean, enabled: Boolean, onClick: () -> Unit) {
  Box(
      modifier =
          Modifier.size(72.dp)
              .clip(CircleShape)
              .background(
                  if (enabled) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
              )
              .clickable(enabled = enabled, onClick = onClick),
      contentAlignment = Alignment.Center,
  ) {
    if (isCapturing) {
      CircularProgressIndicator(
          modifier = Modifier.size(32.dp),
          color = Color.White,
          strokeWidth = 3.dp,
      )
    } else {
      Icon(
          Icons.Default.CameraAlt,
          contentDescription = "Capture",
          tint = Color.White,
          modifier = Modifier.size(36.dp),
      )
    }
  }
}

@Suppress("UnstableCollections") // List<Uri> is only used internally; stability not required here
@Composable
private fun ThumbnailStrip(
    images: List<Uri>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    itemsIndexed(images) { index, uri ->
      Box(modifier = Modifier.size(64.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = "Captured page ${index + 1}",
            modifier =
                Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        // Remove badge
        Box(
            modifier =
                Modifier.size(20.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    .clickable { onRemove(index) },
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              Icons.Default.Close,
              contentDescription = "Remove page ${index + 1}",
              tint = Color.White,
              modifier = Modifier.size(12.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ScanCapContent(onUpgrade: () -> Unit, onClose: () -> Unit) {
  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    IconButton(
        onClick = onClose,
        modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp),
    ) {
      Icon(Icons.Default.Close, contentDescription = "Close")
    }
    Column(
        modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
          Icons.Default.CameraAlt,
          contentDescription = null,
          modifier = Modifier.size(64.dp),
          tint = MaterialTheme.colorScheme.primary,
      )
      Spacer(Modifier.height(16.dp))
      Text("Monthly scan limit reached", style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(8.dp))
      Text(
          text = "Upgrade to Pro for unlimited recipe scans.",
          style =
              MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
          textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(24.dp))
      Button(onClick = onUpgrade) { Text("Upgrade to Pro") }
    }
  }
}

@Composable
private fun PermissionContent(
    permanentlyDenied: Boolean,
    onClose: () -> Unit,
    onAction: () -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    IconButton(
        onClick = onClose,
        modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp),
    ) {
      Icon(Icons.Default.Close, contentDescription = "Close")
    }
    Column(
        modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
          Icons.Default.CameraAlt,
          contentDescription = null,
          modifier = Modifier.size(64.dp),
          tint = MaterialTheme.colorScheme.primary,
      )
      Spacer(Modifier.height(16.dp))
      Text("Camera access required", style = MaterialTheme.typography.titleLarge)
      Spacer(Modifier.height(8.dp))
      Text(
          text =
              if (permanentlyDenied)
                  "Camera permission was denied. Open Settings to allow camera access."
              else "ChompSquad needs camera access to scan your recipes.",
          style =
              MaterialTheme.typography.bodyMedium.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant
              ),
          textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(24.dp))
      Button(onClick = onAction) {
        Text(if (permanentlyDenied) "Open Settings" else "Allow Camera Access")
      }
    }
  }
}
