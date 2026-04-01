package com.bluelampcreative.chompsquad.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import com.bluelampcreative.chompsquad.ui.theme.ChompSquadTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()

  ProfileScreen(
      onHandleEvent = viewModel::handleEvent,
      viewState = viewState,
      modifier = modifier,
  )
}

@Composable
fun ProfileScreen(
    onHandleEvent: (ProfileUiEvent) -> Unit,
    viewState: ProfileViewState,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val photoPicker =
      rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
          val bytes =
              withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
              } ?: return@launch
          val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
          onHandleEvent(ProfileUiEvent.AvatarSelected(bytes, mimeType))
        }
      }

  Box(modifier = modifier.fillMaxSize()) {
    when {
      viewState.isLoading && viewState.profile == null -> {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
      else -> {
        LazyColumn(
            contentPadding =
                PaddingValues(horizontal = ChompSpacing.md, vertical = ChompSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ChompSpacing.md),
        ) {
          item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
            )
          }

          viewState.profile?.let { profile ->
            item {
              IdentityCard(
                  profile = profile,
                  isUploadingAvatar = viewState.isUploadingAvatar,
                  onCameraClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  },
              )
            }

            item { StatsCard(profile = profile) }

            item {
              SettingsRows(
                  onHandleEvent = onHandleEvent,
              )
            }
          }
        }
      }
    }
  }

  if (viewState.isFeedbackDialogVisible) {
    FeedbackDialog(
        message = viewState.feedbackMessage,
        isSubmitting = viewState.isSubmittingFeedback,
        onMessageChange = { onHandleEvent(ProfileUiEvent.FeedbackMessageChanged(it)) },
        onSubmit = { onHandleEvent(ProfileUiEvent.SubmitFeedback) },
        onDismiss = { onHandleEvent(ProfileUiEvent.DismissFeedbackDialog) },
    )
  }

  if (viewState.errorMessage != null) {
    AlertDialog(
        onDismissRequest = { onHandleEvent(ProfileUiEvent.DismissError) },
        title = { Text("Something went wrong") },
        text = { Text(viewState.errorMessage) },
        confirmButton = {
          TextButton(onClick = { onHandleEvent(ProfileUiEvent.DismissError) }) { Text("OK") }
        },
    )
  }
}

@Composable
private fun IdentityCard(
    profile: UserProfileUiModel,
    isUploadingAvatar: Boolean,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChompSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
    ) {
      Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier =
                Modifier.size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
          if (profile.avatarUrl != null) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "Profile avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
          } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (isUploadingAvatar) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(
                  modifier = Modifier.size(32.dp),
                  color = Color.White,
              )
            }
          }
        }
        IconButton(
            onClick = onCameraClick,
            enabled = !isUploadingAvatar,
            modifier =
                Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        ) {
          Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Change avatar",
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.onPrimary,
          )
        }
      }

      Spacer(modifier = Modifier.height(ChompSpacing.xs))

      Text(
          text = profile.displayName,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
      )
      Text(
          text = profile.email,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (profile.isDeveloper) {
        // Non-interactive badge — no action associated with developer tier display.
        // Task 1.6 will gate pro features; this chip is informational only.
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.inverseSurface,
        ) {
          Text(
              text = "Developer",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.inverseOnSurface,
              modifier = Modifier.padding(horizontal = ChompSpacing.sm, vertical = ChompSpacing.xs),
          )
        }
      }
    }
  }
}

@Composable
private fun StatsCard(
    profile: UserProfileUiModel,
    modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(ChompSpacing.md),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = profile.scansUsedThisMonth.toString(),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Scans Used",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      VerticalDivider(modifier = Modifier.height(48.dp))

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (profile.scansRemaining == null) {
          Text(
              text = "∞",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.primary,
          )
        } else {
          Text(
              text = profile.scansRemaining.toString(),
              style = MaterialTheme.typography.headlineMedium,
          )
        }
        Text(
            text = "Scans Remaining",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun SettingsRows(
    onHandleEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm)) {
    if (com.bluelampcreative.chompsquad.BuildConfig.DEBUG) {
      ElevatedCard(
          onClick = { onHandleEvent(ProfileUiEvent.NavigateToDeveloperSettings) },
          colors =
              CardDefaults.elevatedCardColors(
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              ),
          modifier = Modifier.fillMaxWidth(),
      ) {
        ListItem(
            headlineContent = { Text("Developer Settings") },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
      }
    }

    ElevatedCard(
        onClick = { onHandleEvent(ProfileUiEvent.ShowFeedbackDialog) },
        modifier = Modifier.fillMaxWidth(),
    ) {
      ListItem(
          headlineContent = { Text("Send Feedback") },
          trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
      )
    }

    ElevatedCard(
        onClick = { onHandleEvent(ProfileUiEvent.NavigateToSettings) },
        modifier = Modifier.fillMaxWidth(),
    ) {
      ListItem(
          headlineContent = { Text("Settings") },
          trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
      )
    }
  }
}

@Composable
private fun FeedbackDialog(
    message: String,
    isSubmitting: Boolean,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Send Feedback") },
      text = {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            label = { Text("Your feedback") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        TextButton(
            onClick = onSubmit,
            enabled = !isSubmitting && message.isNotBlank(),
        ) {
          Text("Send")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
  ChompSquadTheme {
    ProfileScreen(
        onHandleEvent = {},
        viewState =
            ProfileViewState(
                isLoading = false,
                profile =
                    UserProfileUiModel(
                        id = "preview-user-id",
                        email = "chef@chompsquad.app",
                        displayName = "Alex Chef",
                        avatarUrl = null,
                        subscriptionTier = "developer",
                        scansUsedThisMonth = 12,
                        scansRemaining = null,
                        isDeveloper = true,
                    ),
            ),
        modifier = Modifier.fillMaxSize(),
    )
  }
}
