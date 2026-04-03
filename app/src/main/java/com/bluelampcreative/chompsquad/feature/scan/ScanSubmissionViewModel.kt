package com.bluelampcreative.chompsquad.feature.scan

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.mapper.toDomain
import com.bluelampcreative.chompsquad.data.remote.ScanApi
import com.bluelampcreative.chompsquad.data.scanner.ImagePreprocessor
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScanSubmissionViewModel(
    private val scanSessionRepository: ScanSessionRepository,
    private val imagePreprocessor: ImagePreprocessor,
    private val scanApi: ScanApi,
) :
    CoreViewModel<ScanSubmissionViewState, ScanSubmissionAction, ScanSubmissionUiEvent>(
        ScanSubmissionViewState()
    ) {

  init {
    startSubmission()
  }

  override fun reducer(
      state: ScanSubmissionViewState,
      action: ScanSubmissionAction,
  ): ScanSubmissionViewState =
      when (action) {
        is ScanSubmissionAction.PageMessageUpdated -> state.copy(pageMessage = action.message)
        is ScanSubmissionAction.SubmitFailed ->
            state.copy(isSubmitting = false, error = action.message)
        ScanSubmissionAction.RetryStarted -> ScanSubmissionViewState(isSubmitting = true)
      }

  override fun handleEvent(event: ScanSubmissionUiEvent) {
    when (event) {
      ScanSubmissionUiEvent.OnRetry -> {
        state.dispatch(ScanSubmissionAction.RetryStarted)
        startSubmission()
      }
      ScanSubmissionUiEvent.OnClose -> navigate(NavEvent.GoBack)
    }
  }

  private fun startSubmission() {
    viewModelScope.launch {
      val uris = scanSessionRepository.getPendingImages()
      val total = uris.size
      val pages = mutableListOf<ByteArray>()

      for ((index, uri) in uris.withIndex()) {
        state.dispatch(
            ScanSubmissionAction.PageMessageUpdated("Processing page ${index + 1} of $total…")
        )
        val bytes =
            imagePreprocessor.process(uri).getOrElse { error ->
              state.dispatch(
                  ScanSubmissionAction.SubmitFailed(
                      error.message ?: "Failed to process page ${index + 1}"
                  )
              )
              return@launch
            }
        pages.add(bytes)
      }

      state.dispatch(ScanSubmissionAction.PageMessageUpdated("Uploading your scan…"))
      scanApi
          .submitScan(pages)
          .fold(
              onSuccess = { dto ->
                scanSessionRepository.setScanResult(dto.toDomain())
                navigate(NavEvent.NavigateToScanResult)
              },
              onFailure = { error ->
                state.dispatch(
                    ScanSubmissionAction.SubmitFailed(
                        error.message ?: "Upload failed — please try again"
                    )
                )
              },
          )
    }
  }
}
