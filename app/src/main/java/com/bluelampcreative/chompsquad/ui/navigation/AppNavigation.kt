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
import com.bluelampcreative.chompsquad.feature.onboarding.OnboardingScreen
import com.bluelampcreative.chompsquad.feature.signin.SignInScreen

/**
 * Root composable for the app. Sets up a Navigation 3 [NavDisplay] starting at
 * [AppRoute.Onboarding].
 *
 * SignIn is implemented in task 1.2. SignUp and the Main shell are stubs for tasks 1.3 and 3.8
 * respectively.
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
              SignInScreen(
                  onNavigateToMain = {
                    backStack.clear()
                    backStack += AppRoute.Main
                  },
                  onNavigateBack = { backStack.removeLastOrNull() },
              )
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

/** Temporary placeholder composable used until auth screens are implemented in tasks 1.3+. */
@Composable
private fun AuthPlaceholderScreen(label: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = label, style = MaterialTheme.typography.headlineMedium)
  }
}
