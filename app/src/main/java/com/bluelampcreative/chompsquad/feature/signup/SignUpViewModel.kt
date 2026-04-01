package com.bluelampcreative.chompsquad.feature.signup

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
class SignUpViewModel(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : CoreViewModel<SignUpViewState, SignUpAction, SignUpUiEvent>(SignUpViewState()) {

  override fun reducer(state: SignUpViewState, action: SignUpAction): SignUpViewState =
      when (action) {
        is SignUpAction.StartLoading -> state.copy(isLoading = true, errorMessage = null)
        is SignUpAction.ShowError -> state.copy(isLoading = false, errorMessage = action.message)
        is SignUpAction.DismissError -> state.copy(errorMessage = null)
      }

  override fun handleEvent(event: SignUpUiEvent) {
    when (event) {
      is SignUpUiEvent.OnSubmit -> handleSignUp(event.email, event.password)
      is SignUpUiEvent.OnGoogleTokenReceived -> handleGoogleSignIn(event.idToken)
      is SignUpUiEvent.OnGoogleSignInError -> state.dispatch(SignUpAction.ShowError(event.message))
      is SignUpUiEvent.OnDismissError -> state.dispatch(SignUpAction.DismissError)
    }
  }

  private fun handleGoogleSignIn(idToken: String) {
    viewModelScope.launch {
      state.dispatch(SignUpAction.StartLoading)
      authApi
          .signInWithGoogle(idToken)
          .onSuccess { response ->
            tokenRepository.saveTokens(response.accessToken, response.refreshToken)
            subscriptionRepository.refreshEntitlements()
            navigate(NavEvent.NavigateToMain)
          }
          .onFailure { error ->
            state.dispatch(
                SignUpAction.ShowError(
                    error.toAuthErrorMessage("Account creation failed. Please try again.")
                )
            )
          }
    }
  }

  private fun handleSignUp(email: String, password: String) {
    val screenName = email.substringBefore('@').ifBlank { email }
    viewModelScope.launch {
      state.dispatch(SignUpAction.StartLoading)
      authApi
          .signUp(email, password, screenName)
          .onSuccess { response ->
            tokenRepository.saveTokens(response.accessToken, response.refreshToken)
            subscriptionRepository.refreshEntitlements()
            navigate(NavEvent.NavigateToMain)
          }
          .onFailure { error ->
            state.dispatch(
                SignUpAction.ShowError(
                    error.toAuthErrorMessage("Account creation failed. Please try again.")
                )
            )
          }
    }
  }
}
