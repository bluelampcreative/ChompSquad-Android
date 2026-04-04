package com.bluelampcreative.chompsquad.feature.scan

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.mapper.toDomain
import com.bluelampcreative.chompsquad.data.purchases.SubscriptionRepository
import com.bluelampcreative.chompsquad.data.remote.ScanApi
import com.bluelampcreative.chompsquad.data.scanner.ImagePreprocessor
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

private const val TAG = "ScanSubmission"

@KoinViewModel
class ScanSubmissionViewModel(
    private val scanSessionRepository: ScanSessionRepository,
    private val imagePreprocessor: ImagePreprocessor,
    private val scanApi: ScanApi,
    private val subscriptionRepository: SubscriptionRepository,
) :
    CoreViewModel<ScanSubmissionViewState, ScanSubmissionAction, ScanSubmissionUiEvent>(
        ScanSubmissionViewState()
    ) {

  private var submissionJob: Job? = null

  init {
    startSubmission()
  }

  override fun reducer(
      state: ScanSubmissionViewState,
      action: ScanSubmissionAction,
  ): ScanSubmissionViewState =
      when (action) {
        is ScanSubmissionAction.PageMessageUpdated -> state.copy(pageMessage = action.message)
        is ScanSubmissionAction.SubmitFailed -> state.copy(error = action.message)
        ScanSubmissionAction.RetryStarted -> ScanSubmissionViewState()
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
    submissionJob?.cancel()
    submissionJob =
        viewModelScope.launch {
          val uris = scanSessionRepository.getPendingImages()
          if (uris.isEmpty()) {
            state.dispatch(
                ScanSubmissionAction.SubmitFailed(
                    "No images to scan — please go back and try again"
                )
            )
            return@launch
          }

          val total = uris.size
          val pages = mutableListOf<ByteArray>()

          for ((index, uri) in uris.withIndex()) {
            state.dispatch(
                ScanSubmissionAction.PageMessageUpdated("Processing page ${index + 1} of $total…")
            )
            val bytes =
                imagePreprocessor.process(uri).getOrElse { error ->
                  Log.e(TAG, "Failed to preprocess page ${index + 1}", error)
                  state.dispatch(
                      ScanSubmissionAction.SubmitFailed(
                          "Failed to process page ${index + 1} — please try again"
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
                    Log.e(TAG, "Scan upload failed", error)
                    if (
                        error is ClientRequestException &&
                            error.response.status.value in SCAN_CAP_STATUS_CODES
                    ) {
                      // Server confirmed the scan cap is exhausted. Refresh entitlements
                      // (the user may have upgraded on another device) then show the Paywall.
                      viewModelScope.launch { subscriptionRepository.refreshEntitlements() }
                      navigate(NavEvent.NavigateToPaywall)
                    } else {
                      state.dispatch(ScanSubmissionAction.SubmitFailed(error.toUserMessage()))
                    }
                  },
              )
        }
  }
}

private const val HTTP_PAYMENT_REQUIRED = 402
private const val HTTP_FORBIDDEN = 403

// HTTP status codes the server uses to indicate the scan quota is exhausted.
private val SCAN_CAP_STATUS_CODES = setOf(HTTP_PAYMENT_REQUIRED, HTTP_FORBIDDEN)

private fun Throwable.toUserMessage(): String =
    when (this) {
      is HttpRequestTimeoutException -> "The server took too long to respond — please try again"
      is ServerResponseException -> "Our servers are having issues — please try again shortly"
      is ClientRequestException -> "Something went wrong — please try again"
      is IOException -> "Network error — check your connection and try again"
      else -> "Something went wrong — please try again"
    }
