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

  /**
   * Clear the entire back stack and show Sign In. Used after sign-out or account deletion so
   * authenticated screens are not reachable via back navigation.
   */
  data object NavigateToSignInClearStack : NavEvent

  /** Push the Camera Capture screen (task 2.1). */
  data object NavigateToCameraCapture : NavEvent

  /** Push the Scan Submission screen (task 2.3). */
  data object NavigateToScanSubmission : NavEvent

  /** Replace the Scan Submission screen with the Scan Result screen (task 2.4). */
  data object NavigateToScanResult : NavEvent

  /** Push the Ingredient Editor screen (task 2.5). */
  data object NavigateToIngredientEditor : NavEvent

  /** Push the Step Editor screen (task 2.6). */
  data object NavigateToStepEditor : NavEvent

  /** Push the Manual Entry screen (task 2.9). */
  data object NavigateToManualEntry : NavEvent

  /** Push the Recipe Detail screen (task 3.2). */
  data class NavigateToRecipeDetail(val id: String) : NavEvent
}
