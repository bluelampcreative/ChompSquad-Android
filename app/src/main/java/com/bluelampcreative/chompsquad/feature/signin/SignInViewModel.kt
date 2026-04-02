package com.bluelampcreative.chompsquad.feature.signin

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.purchases.SubscriptionRepository
import com.bluelampcreative.chompsquad.data.remote.AuthApi
import com.bluelampcreative.chompsquad.data.remote.toAuthErrorMessage
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SignInViewModel(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : CoreViewModel<SignInViewState, SignInAction, SignInUiEvent>(SignInViewState()) {

  override fun reducer(state: SignInViewState, action: SignInAction): SignInViewState =
      when (action) {
        is SignInAction.StartLoading -> state.copy(isLoading = true, errorMessage = null)
        is SignInAction.ShowError -> state.copy(isLoading = false, errorMessage = action.message)
        is SignInAction.DismissError -> state.copy(errorMessage = null)
      }

  override fun handleEvent(event: SignInUiEvent) {
    when (event) {
      is SignInUiEvent.OnEmailSignInSubmitted -> signInWithEmail(event.email, event.password)
      is SignInUiEvent.OnGoogleTokenReceived -> signInWithGoogle(event.idToken)
      is SignInUiEvent.OnSignInError -> state.dispatch(SignInAction.ShowError(event.message))
      is SignInUiEvent.OnDismissError -> state.dispatch(SignInAction.DismissError)
    }
  }

  private fun signInWithEmail(email: String, password: String) {
    viewModelScope.launch {
      state.dispatch(SignInAction.StartLoading)
      val response =
          authApi.signInWithEmail(email, password).getOrElse { error ->
            state.dispatch(
                SignInAction.ShowError(
                    error.toAuthErrorMessage("Sign in failed. Please try again.")
                )
            )
            return@launch
          }
      tokenRepository.saveTokens(response.accessToken, response.refreshToken)
      subscriptionRepository.refreshEntitlements()
      navigate(NavEvent.NavigateToMain)
    }
  }

  /**
   * Exchanges a Google ID token for a ChompSquad access + refresh token pair, then navigates to the
   * main screen on success.
   *
   * The Google ID token is obtained by the composable via the Credential Manager API (which
   * requires an [android.app.Activity] context). The composable hands the token here via
   * [handleEvent] once the platform flow completes.
   */
  private fun signInWithGoogle(idToken: String) {
    viewModelScope.launch {
      state.dispatch(SignInAction.StartLoading)
      val response =
          authApi.signInWithGoogle(idToken).getOrElse { error ->
            state.dispatch(
                SignInAction.ShowError(
                    error.toAuthErrorMessage("Sign in failed. Please try again.")
                )
            )
            return@launch
          }
      tokenRepository.saveTokens(response.accessToken, response.refreshToken)
      subscriptionRepository.refreshEntitlements()
      navigate(NavEvent.NavigateToMain)
    }
  }
}
