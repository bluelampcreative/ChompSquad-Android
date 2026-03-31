package com.bluelampcreative.chompsquad.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.bluelampcreative.chompsquad.ui.onboarding.OnboardingScreen

/**
 * Root composable for the app. Sets up a Navigation 3 [NavDisplay] starting at
 * [AppRoute.Onboarding].
 *
 * Auth destinations (SignIn, SignUp) are wired here as stubs; they will be implemented in tasks 1.2
 * and 1.3. The Main shell (bottom-nav tabs) is likewise a stub for task 3.8.
 */
@Suppress("ModifierMissing") // Root navigation composable — no modifier parameter needed
@Composable
fun ChompSquadApp() {
  val backStack = rememberNavBackStack(AppRoute.Onboarding)

  NavDisplay(
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryProvider =
          entryProvider {
            entry<AppRoute.Onboarding> {
              OnboardingScreen(
                  onNavigateToSignIn = { backStack += AppRoute.SignIn },
                  onNavigateToSignUp = { backStack += AppRoute.SignUp },
              )
            }
            entry<AppRoute.SignIn> {
              // TODO(task 1.2 / 1.3): implement Sign-In screen
              AuthPlaceholderScreen(label = "Sign In")
            }
            entry<AppRoute.SignUp> {
              // TODO(task 1.3): implement Sign-Up screen
              AuthPlaceholderScreen(label = "Sign Up")
            }
            entry<AppRoute.Main> {
              // TODO(task 3.8): implement bottom-nav shell
              AuthPlaceholderScreen(label = "Main App")
            }
          },
  )
}

/** Temporary placeholder composable used until auth screens are implemented in tasks 1.2–1.3. */
@Composable
private fun AuthPlaceholderScreen(label: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = label, style = MaterialTheme.typography.headlineMedium)
  }
}
