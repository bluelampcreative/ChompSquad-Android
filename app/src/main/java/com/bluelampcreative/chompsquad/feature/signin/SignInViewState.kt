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

/** One-shot navigation events emitted by [SignInViewModel]. */
sealed interface SignInNavEvent {
  data object NavigateToMain : SignInNavEvent
}
