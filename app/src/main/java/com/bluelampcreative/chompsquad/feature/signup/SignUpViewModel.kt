package com.bluelampcreative.chompsquad.feature.signup

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.remote.AuthApi
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SignUpViewModel(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
) : CoreViewModel<SignUpViewState, SignUpAction, SignUpUiEvent>(SignUpViewState()) {

  override fun reducer(state: SignUpViewState, action: SignUpAction): SignUpViewState =
      when (action) {
        is SignUpAction.StartLoading -> state.copy(isLoading = true, errorMessage = null)
        is SignUpAction.ShowError -> state.copy(isLoading = false, errorMessage = action.message)
        is SignUpAction.DismissError -> state.copy(errorMessage = null)
      }

  override fun handleEvent(event: SignUpUiEvent) {
    when (event) {
      is SignUpUiEvent.OnSubmit -> signUp(event.email, event.password)
      is SignUpUiEvent.OnDismissError -> state.dispatch(SignUpAction.DismissError)
    }
  }

  private fun signUp(email: String, password: String) {
    val screenName = email.substringBefore('@').ifBlank { email }
    viewModelScope.launch {
      state.dispatch(SignUpAction.StartLoading)
      authApi
          .signUp(email, password, screenName)
          .onSuccess { response ->
            tokenRepository.saveTokens(response.accessToken, response.refreshToken)
            navigate(NavEvent.NavigateToMain)
          }
          .onFailure { error ->
            state.dispatch(
                SignUpAction.ShowError(
                    error.message ?: "Account creation failed. Please try again."
                )
            )
          }
    }
  }
}
