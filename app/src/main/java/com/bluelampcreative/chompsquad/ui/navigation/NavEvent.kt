package com.bluelampcreative.chompsquad.ui.navigation

sealed interface NavEvent {
  data object GoBack : NavEvent

  data object NavigateToMain : NavEvent

  /** Replace the current auth screen with Sign In (pop current, push Sign In). */
  data object NavigateToSignIn : NavEvent

  /** Replace the current auth screen with Sign Up (pop current, push Sign Up). */
  data object NavigateToSignUp : NavEvent

  /** Push the Settings screen (task 1.8). */
  data object NavigateToSettings : NavEvent

  /** Push the Developer Settings screen (debug builds only). */
  data object NavigateToDeveloperSettings : NavEvent

  /** Push the Paywall screen. */
  data object NavigateToPaywall : NavEvent
}
