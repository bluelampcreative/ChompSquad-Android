package com.bluelampcreative.chompsquad.feature.settings

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.purchases.SubscriptionRepository
import com.bluelampcreative.chompsquad.data.remote.AuthApi
import com.bluelampcreative.chompsquad.data.remote.UserProfileApi
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val userProfileApi: UserProfileApi,
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : CoreViewModel<SettingsViewState, SettingsAction, SettingsUiEvent>(SettingsViewState()) {

  init {
    loadProfile()
    subscriptionRepository.entitlementStatus
        .onEach { status ->
          state.dispatch(SettingsAction.EntitlementLoaded(hasPro = status.hasPro))
        }
        .launchIn(viewModelScope)
  }

  override fun reducer(state: SettingsViewState, action: SettingsAction): SettingsViewState =
      when (action) {
        is SettingsAction.ProfileLoaded ->
            state.copy(email = action.email, displayName = action.displayName)
        is SettingsAction.EntitlementLoaded -> state.copy(hasPro = action.hasPro)
        is SettingsAction.ShowSignOutDialog -> state.copy(showSignOutDialog = true)
        is SettingsAction.DismissSignOutDialog -> state.copy(showSignOutDialog = false)
        is SettingsAction.SignOutStarted ->
            state.copy(isSigningOut = true, showSignOutDialog = false)
        is SettingsAction.ShowDeleteAccountDialog -> state.copy(showDeleteAccountDialog = true)
        is SettingsAction.DismissDeleteAccountDialog -> state.copy(showDeleteAccountDialog = false)
        is SettingsAction.DeleteAccountStarted ->
            state.copy(isDeletingAccount = true, showDeleteAccountDialog = false)
        is SettingsAction.RestorePurchasesStarted -> state.copy(isRestoringPurchases = true)
        is SettingsAction.RestorePurchasesFinished -> state.copy(isRestoringPurchases = false)
        is SettingsAction.ShowError ->
            state.copy(
                isSigningOut = false,
                isDeletingAccount = false,
                isRestoringPurchases = false,
                errorMessage = action.message,
            )
        is SettingsAction.DismissError -> state.copy(errorMessage = null)
      }

  override fun handleEvent(event: SettingsUiEvent) {
    when (event) {
      SettingsUiEvent.OnBack -> navigate(NavEvent.GoBack)
      SettingsUiEvent.OnSignOut -> state.dispatch(SettingsAction.ShowSignOutDialog)
      SettingsUiEvent.OnConfirmSignOut -> signOut()
      SettingsUiEvent.OnDismissSignOutDialog -> state.dispatch(SettingsAction.DismissSignOutDialog)
      SettingsUiEvent.OnDeleteAccount -> state.dispatch(SettingsAction.ShowDeleteAccountDialog)
      SettingsUiEvent.OnConfirmDeleteAccount -> deleteAccount()
      SettingsUiEvent.OnDismissDeleteAccountDialog ->
          state.dispatch(SettingsAction.DismissDeleteAccountDialog)
      SettingsUiEvent.OnRestorePurchases -> restorePurchases()
      SettingsUiEvent.OnUpgradeToPro -> navigate(NavEvent.NavigateToPaywall)
      SettingsUiEvent.OnManageProfile -> navigate(NavEvent.GoBack)
      SettingsUiEvent.OnDismissError -> state.dispatch(SettingsAction.DismissError)
      // URL-opening events are handled in the composable (require Context).
      SettingsUiEvent.OnNotifications,
      SettingsUiEvent.OnManageBilling,
      SettingsUiEvent.OnContactSupport,
      SettingsUiEvent.OnPrivacyPolicy,
      SettingsUiEvent.OnTermsOfService -> Unit
    }
  }

  private fun loadProfile() {
    viewModelScope.launch {
      val result = userProfileApi.getProfile()
      if (result.isFailure) return@launch // retain empty defaults on error
      val profile = result.getOrThrow()
      state.dispatch(SettingsAction.ProfileLoaded(profile.email, profile.displayName))
    }
  }

  private fun signOut() {
    viewModelScope.launch {
      state.dispatch(SettingsAction.SignOutStarted)
      val refreshToken = tokenRepository.getRefreshToken()
      if (refreshToken != null) {
        // Best-effort server-side logout — don't block local sign-out on network failure.
        authApi.logout(refreshToken)
      }
      tokenRepository.clearTokens()
      navigate(NavEvent.NavigateToSignInClearStack)
    }
  }

  private fun deleteAccount() {
    viewModelScope.launch {
      state.dispatch(SettingsAction.DeleteAccountStarted)
      val result = authApi.deleteAccount()
      if (result.isFailure) {
        state.dispatch(
            SettingsAction.ShowError(
                result.exceptionOrNull()?.message ?: "Failed to delete account. Please try again."
            )
        )
        return@launch
      }
      tokenRepository.clearTokens()
      navigate(NavEvent.NavigateToSignInClearStack)
    }
  }

  private fun restorePurchases() {
    viewModelScope.launch {
      state.dispatch(SettingsAction.RestorePurchasesStarted)
      val result = subscriptionRepository.restorePurchases()
      if (result.isFailure) {
        state.dispatch(
            SettingsAction.ShowError(
                result.exceptionOrNull()?.message
                    ?: "Failed to restore purchases. Please try again."
            )
        )
        return@launch
      }
      state.dispatch(SettingsAction.RestorePurchasesFinished)
    }
  }
}
