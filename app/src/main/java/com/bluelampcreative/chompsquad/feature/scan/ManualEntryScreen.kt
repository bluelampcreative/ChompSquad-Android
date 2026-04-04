package com.bluelampcreative.chompsquad.feature.scan

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

private const val SAVE_SUCCESS_DELAY_MS = 800L

@Suppress(
    "ModifierMissing",
    "ComposeModifierMissing",
) // Root navigation composable — no modifier parameter needed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onNavEvent: (NavEvent) -> Unit,
    viewModel: ManualEntryViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  val haptic = LocalHapticFeedback.current
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  // Haptic + brief delay before navigating away so the spring animation is visible.
  LaunchedEffect(viewState.saveSuccess) {
    if (viewState.saveSuccess) {
      haptic.performHapticFeedback(HapticFeedbackType.Confirm)
      delay(SAVE_SUCCESS_DELAY_MS)
      viewModel.handleEvent(ScanResultUiEvent.OnNavigateAfterSave)
    }
  }

  // Show save errors as a transient snackbar.
  val saveError = viewState.saveError
  LaunchedEffect(saveError) {
    if (saveError != null) {
      snackbarHostState.showSnackbar(saveError)
    }
  }

  val fabScale by
      animateFloatAsState(
          targetValue = if (viewState.saveSuccess) 1.25f else 1f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioLowBouncy,
                  stiffness = Spring.StiffnessMediumLow,
              ),
          label = "fabScale",
      )

  Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
            title = { Text("New Recipe") },
            navigationIcon = {
              IconButton(onClick = { viewModel.handleEvent(ScanResultUiEvent.OnClose) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
              }
            },
        )
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = {
              if (!viewState.isSaving && !viewState.saveSuccess) {
                viewModel.handleEvent(ScanResultUiEvent.OnSave)
              }
            },
            icon = {
              if (viewState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
              } else {
                Icon(Icons.Default.Check, contentDescription = null)
              }
            },
            text = { Text(if (viewState.saveSuccess) "Saved!" else "Save") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.scale(fabScale),
        )
      },
  ) { innerPadding ->
    ScanResultContent(
        viewState = viewState,
        onEvent = { viewModel.handleEvent(it) },
        modifier = Modifier.padding(innerPadding),
    )
  }
}
