package com.bluelampcreative.chompsquad.feature.profile

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.BuildConfig
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.remote.UserProfileApi
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val userProfileApi: UserProfileApi,
) : CoreViewModel<ProfileViewState, ProfileAction, ProfileUiEvent>(ProfileViewState()) {

  init {
    loadProfile()
  }

  override fun reducer(state: ProfileViewState, action: ProfileAction): ProfileViewState =
      when (action) {
        is ProfileAction.StartLoading -> state.copy(isLoading = true, errorMessage = null)
        is ProfileAction.ProfileLoaded -> state.copy(isLoading = false, profile = action.profile)
        is ProfileAction.AvatarUploadStarted -> state.copy(isUploadingAvatar = true)
        is ProfileAction.AvatarUploadFinished -> state.copy(isUploadingAvatar = false)
        is ProfileAction.ShowFeedbackDialog ->
            state.copy(isFeedbackDialogVisible = true, feedbackMessage = "")
        is ProfileAction.DismissFeedbackDialog ->
            state.copy(isFeedbackDialogVisible = false, feedbackMessage = "")
        is ProfileAction.FeedbackMessageChanged -> state.copy(feedbackMessage = action.message)
        is ProfileAction.FeedbackSubmitStarted -> state.copy(isSubmittingFeedback = true)
        is ProfileAction.FeedbackSubmitFinished ->
            state.copy(
                isSubmittingFeedback = false,
                isFeedbackDialogVisible = false,
                feedbackMessage = "",
            )
        is ProfileAction.ShowError -> state.copy(isLoading = false, errorMessage = action.message)
        is ProfileAction.DismissError -> state.copy(errorMessage = null)
      }

  override fun handleEvent(event: ProfileUiEvent) {
    when (event) {
      is ProfileUiEvent.Refresh -> loadProfile()
      is ProfileUiEvent.AvatarSelected -> uploadAvatar(event.imageBytes, event.mimeType)
      is ProfileUiEvent.ShowFeedbackDialog -> state.dispatch(ProfileAction.ShowFeedbackDialog)
      is ProfileUiEvent.DismissFeedbackDialog -> state.dispatch(ProfileAction.DismissFeedbackDialog)
      is ProfileUiEvent.FeedbackMessageChanged ->
          state.dispatch(ProfileAction.FeedbackMessageChanged(event.message))
      is ProfileUiEvent.SubmitFeedback -> submitFeedback()
      is ProfileUiEvent.NavigateToSettings -> navigate(NavEvent.NavigateToSettings)
      is ProfileUiEvent.NavigateToDeveloperSettings ->
          navigate(NavEvent.NavigateToDeveloperSettings)
      is ProfileUiEvent.DismissError -> state.dispatch(ProfileAction.DismissError)
    }
  }

  private fun loadProfile() {
    viewModelScope.launch {
      state.dispatch(ProfileAction.StartLoading)
      userProfileApi
          .getProfile()
          .onSuccess { dto ->
            state.dispatch(
                ProfileAction.ProfileLoaded(
                    UserProfileUiModel(
                        id = dto.id,
                        email = dto.email,
                        displayName = dto.displayName,
                        avatarUrl = dto.avatarUrl,
                        subscriptionTier = dto.subscriptionTier,
                        scansUsedThisMonth = dto.scansUsedThisMonth,
                        scansRemaining = dto.scansRemaining,
                        isDeveloper = dto.subscriptionTier == "developer" || BuildConfig.DEBUG,
                    )
                )
            )
          }
          .onFailure {
            state.dispatch(ProfileAction.ShowError("Failed to load profile. Please try again."))
          }
    }
  }

  private fun uploadAvatar(imageBytes: ByteArray, mimeType: String) {
    viewModelScope.launch {
      state.dispatch(ProfileAction.AvatarUploadStarted)
      userProfileApi
          .uploadAvatar(imageBytes, mimeType)
          .onSuccess { loadProfile() }
          .onFailure {
            state.dispatch(ProfileAction.AvatarUploadFinished)
            state.dispatch(ProfileAction.ShowError("Avatar upload failed. Please try again."))
          }
    }
  }

  private fun submitFeedback() {
    val message = state.value.feedbackMessage.trim()
    if (message.isBlank()) return
    viewModelScope.launch {
      state.dispatch(ProfileAction.FeedbackSubmitStarted)
      userProfileApi
          .submitFeedback(message)
          .onSuccess { state.dispatch(ProfileAction.FeedbackSubmitFinished) }
          .onFailure {
            state.dispatch(ProfileAction.FeedbackSubmitFinished)
            state.dispatch(ProfileAction.ShowError("Failed to send feedback. Please try again."))
          }
    }
  }
}
