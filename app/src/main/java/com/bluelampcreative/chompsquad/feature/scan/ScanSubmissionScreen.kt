package com.bluelampcreative.chompsquad.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import org.koin.androidx.compose.koinViewModel

private val IndicatorSize = 56.dp

@Composable
fun ScanSubmissionScreen(
    onNavEvent: (NavEvent) -> Unit,
    viewModel: ScanSubmissionViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  ScanSubmissionContent(
      viewState = viewState,
      onRetry = { viewModel.handleEvent(ScanSubmissionUiEvent.OnRetry) },
      onClose = { viewModel.handleEvent(ScanSubmissionUiEvent.OnClose) },
  )
}

@Composable
private fun ScanSubmissionContent(
    viewState: ScanSubmissionViewState,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxSize().padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    if (viewState.error != null) {
      ErrorContent(message = viewState.error, onRetry = onRetry, onClose = onClose)
    } else {
      LoadingContent(pageMessage = viewState.pageMessage)
    }
  }
}

@Composable
private fun LoadingContent(pageMessage: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    CircularProgressIndicator(modifier = Modifier.size(IndicatorSize))
    if (pageMessage.isNotBlank()) {
      Spacer(modifier = Modifier.height(24.dp))
      Text(
          text = pageMessage,
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onClose: () -> Unit) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onRetry) { Text("Retry") }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onClose) { Text("Close") }
  }
}
