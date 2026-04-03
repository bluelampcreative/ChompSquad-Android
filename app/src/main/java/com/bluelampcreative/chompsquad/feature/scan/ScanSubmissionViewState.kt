package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.core.ViewAction

data class ScanSubmissionViewState(
    val pageMessage: String = "",
    val error: String? = null,
)

sealed interface ScanSubmissionAction : ViewAction {
  data class PageMessageUpdated(val message: String) : ScanSubmissionAction

  data class SubmitFailed(val message: String) : ScanSubmissionAction

  data object RetryStarted : ScanSubmissionAction
}

sealed interface ScanSubmissionUiEvent {
  data object OnRetry : ScanSubmissionUiEvent

  data object OnClose : ScanSubmissionUiEvent
}
