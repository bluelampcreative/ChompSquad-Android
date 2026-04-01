package com.bluelampcreative.chompsquad.feature.signin

import com.bluelampcreative.chompsquad.core.ViewAction

data class SignInViewState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SignInAction : ViewAction {
  data object StartLoading : SignInAction

  data class ShowError(val message: String) : SignInAction

  data object DismissError : SignInAction
}

/**
 * UI events flowing FROM the composable INTO [SignInViewModel].
 * - [OnEmailSignInSubmitted]: User tapped "Sign In" with email credentials.
 * - [OnGoogleTokenReceived]: Credential Manager yielded a Google ID token; begin the backend
 *   exchange.
 * - [OnSignInError]: A platform-level error occurred during the Credential Manager flow (wrong
 *   credential type, unexpected failure, etc.).
 * - [OnDismissError]: The user dismissed the error dialog.
 */
sealed interface SignInUiEvent {
  data class OnEmailSignInSubmitted(val email: String, val password: String) : SignInUiEvent

  data class OnGoogleTokenReceived(val idToken: String) : SignInUiEvent

  data class OnSignInError(val message: String) : SignInUiEvent

  data object OnDismissError : SignInUiEvent
}
