package com.bluelampcreative.chompsquad.feature.signup

import com.bluelampcreative.chompsquad.core.ViewAction

data class SignUpViewState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SignUpAction : ViewAction {
  data object StartLoading : SignUpAction

  data class ShowError(val message: String) : SignUpAction

  data object DismissError : SignUpAction
}

/**
 * UI events flowing FROM the composable INTO [SignUpViewModel].
 * - [OnSubmit]: User tapped "Create account". Screen-name is derived from the email local part by
 *   the ViewModel (e.g. "johnny" from "johnny@email.com").
 * - [OnGoogleTokenReceived]: Credential Manager yielded a Google ID token; the backend creates the
 *   account if one doesn't exist, so the same endpoint handles sign-up and sign-in for Google.
 * - [OnGoogleSignInError]: A platform-level error occurred during the Credential Manager flow.
 * - [OnDismissError]: The user dismissed the error dialog.
 */
sealed interface SignUpUiEvent {
  data class OnSubmit(val email: String, val password: String) : SignUpUiEvent

  data class OnGoogleTokenReceived(val idToken: String) : SignUpUiEvent

  data class OnGoogleSignInError(val message: String) : SignUpUiEvent

  data object OnDismissError : SignUpUiEvent
}
