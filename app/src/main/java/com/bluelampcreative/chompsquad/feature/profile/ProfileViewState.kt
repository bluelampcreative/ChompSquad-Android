package com.bluelampcreative.chompsquad.feature.profile

import com.bluelampcreative.chompsquad.core.ViewAction

data class UserProfileUiModel(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val subscriptionTier: String,
    val scansUsedThisMonth: Int,
    val scansRemaining: Int?,
    val isDeveloper: Boolean,
)

data class ProfileViewState(
    val isLoading: Boolean = true,
    val profile: UserProfileUiModel? = null,
    val isUploadingAvatar: Boolean = false,
    val isFeedbackDialogVisible: Boolean = false,
    val feedbackMessage: String = "",
    val isSubmittingFeedback: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ProfileAction : ViewAction {
  data object StartLoading : ProfileAction

  data class ProfileLoaded(val profile: UserProfileUiModel) : ProfileAction

  data object AvatarUploadStarted : ProfileAction

  data object AvatarUploadFinished : ProfileAction

  data object ShowFeedbackDialog : ProfileAction

  data object DismissFeedbackDialog : ProfileAction

  data class FeedbackMessageChanged(val message: String) : ProfileAction

  data object FeedbackSubmitStarted : ProfileAction

  data object FeedbackSubmitFinished : ProfileAction

  data class ShowError(val message: String) : ProfileAction

  data object DismissError : ProfileAction
}

sealed interface ProfileUiEvent {
  data object Refresh : ProfileUiEvent

  data class AvatarSelected(val imageBytes: ByteArray, val mimeType: String) : ProfileUiEvent

  data object ShowFeedbackDialog : ProfileUiEvent

  data object DismissFeedbackDialog : ProfileUiEvent

  data class FeedbackMessageChanged(val message: String) : ProfileUiEvent

  data object SubmitFeedback : ProfileUiEvent

  data object NavigateToSettings : ProfileUiEvent

  data object NavigateToDeveloperSettings : ProfileUiEvent

  data object DismissError : ProfileUiEvent
}
