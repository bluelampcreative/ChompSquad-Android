package com.bluelampcreative.chompsquad.feature.signin

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.remote.AuthApi
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
) : CoreViewModel<SignInViewState, SignInAction, SignInNavEvent>(SignInViewState()) {

  override fun reducer(state: SignInViewState, action: SignInAction): SignInViewState =
      when (action) {
        is SignInAction.StartLoading -> state.copy(isLoading = true, errorMessage = null)
        is SignInAction.ShowError -> state.copy(isLoading = false, errorMessage = action.message)
        is SignInAction.DismissError -> state.copy(errorMessage = null)
      }

  /**
   * Called from the composable once the Credential Manager yields a Google ID token. Sends the
   * token to the backend and stores the resulting token pair via [TokenRepository].
   */
  fun signInWithGoogle(idToken: String) {
    viewModelScope.launch {
      state.dispatch(SignInAction.StartLoading)
      authApi
          .signInWithGoogle(idToken)
          .onSuccess { response ->
            tokenRepository.saveTokens(response.accessToken, response.refreshToken)
            navigate(SignInNavEvent.NavigateToMain)
          }
          .onFailure { error ->
            state.dispatch(
                SignInAction.ShowError(error.message ?: "Sign in failed. Please try again.")
            )
          }
    }
  }

  fun dismissError() = state.dispatch(SignInAction.DismissError)

  override fun handleEvent(event: SignInNavEvent) = Unit
}
