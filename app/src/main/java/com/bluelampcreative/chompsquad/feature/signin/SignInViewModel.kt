package com.bluelampcreative.chompsquad.feature.signin

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.remote.AuthApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
) : CoreViewModel<SignInViewState, SignInAction, Unit>(SignInViewState()) {

  private val _navEvents = Channel<SignInNavEvent>(Channel.BUFFERED)

  /** Collect in the composable with [LaunchedEffect] to handle one-shot navigation. */
  val navEvents = _navEvents.receiveAsFlow()

  override fun reducer(state: SignInViewState, action: SignInAction): SignInViewState =
      when (action) {
        SignInAction.StartLoading -> state.copy(isLoading = true, errorMessage = null)
        is SignInAction.ShowError -> state.copy(isLoading = false, errorMessage = action.message)
        SignInAction.DismissError -> state.copy(errorMessage = null)
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
            _navEvents.send(SignInNavEvent.NavigateToMain)
          }
          .onFailure { error ->
            state.dispatch(
                SignInAction.ShowError(error.message ?: "Sign in failed. Please try again.")
            )
          }
    }
  }

  fun dismissError() = state.dispatch(SignInAction.DismissError)

  // UIEventHandler — not used for this screen; navigation is driven by navEvents.
  override fun handleEvent(event: Unit) = Unit
}
