package com.bluelampcreative.chompsquad.feature.settings

import com.bluelampcreative.chompsquad.core.ViewAction

data class SettingsViewState(
    val email: String = "",
    val displayName: String = "",
    val hasPro: Boolean = false,
    val isSigningOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val isRestoringPurchases: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SettingsAction : ViewAction {
  data class ProfileLoaded(val email: String, val displayName: String) : SettingsAction

  data class EntitlementLoaded(val hasPro: Boolean) : SettingsAction

  data object ShowSignOutDialog : SettingsAction

  data object DismissSignOutDialog : SettingsAction

  data object SignOutStarted : SettingsAction

  data object ShowDeleteAccountDialog : SettingsAction

  data object DismissDeleteAccountDialog : SettingsAction

  data object DeleteAccountStarted : SettingsAction

  data object RestorePurchasesStarted : SettingsAction

  data object RestorePurchasesFinished : SettingsAction

  data class ShowError(val message: String) : SettingsAction

  data object DismissError : SettingsAction
}

sealed interface SettingsUiEvent {
  data object OnBack : SettingsUiEvent

  data object OnSignOut : SettingsUiEvent

  data object OnConfirmSignOut : SettingsUiEvent

  data object OnDismissSignOutDialog : SettingsUiEvent

  data object OnDeleteAccount : SettingsUiEvent

  data object OnConfirmDeleteAccount : SettingsUiEvent

  data object OnDismissDeleteAccountDialog : SettingsUiEvent

  data object OnRestorePurchases : SettingsUiEvent

  data object OnUpgradeToPro : SettingsUiEvent

  data object OnEditProfile : SettingsUiEvent

  data object OnNotifications : SettingsUiEvent

  data object OnManageBilling : SettingsUiEvent

  data object OnContactSupport : SettingsUiEvent

  data object OnPrivacyPolicy : SettingsUiEvent

  data object OnTermsOfService : SettingsUiEvent

  data object OnDismissError : SettingsUiEvent
}
