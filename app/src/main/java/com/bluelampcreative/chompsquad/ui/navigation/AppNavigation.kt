package com.bluelampcreative.chompsquad.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.remote.AuthEventBus
import com.bluelampcreative.chompsquad.feature.onboarding.OnboardingScreen
import com.bluelampcreative.chompsquad.feature.signin.SignInScreen
import com.bluelampcreative.chompsquad.feature.signup.SignUpScreen
import org.koin.compose.koinInject

/**
 * Root composable for the app. Checks for a persisted token before building the backstack so
 * returning users land directly on [AppRoute.Main] rather than re-running the auth flow.
 *
 * Renders nothing until the DataStore read resolves (typically < 1 frame). The Main shell is a stub
 * for task 3.8.
 */
@Suppress("ModifierMissing") // Root navigation composable — no modifier parameter needed
@Composable
fun ChompSquadApp() {
  val tokenRepository = koinInject<TokenRepository>()
  val authEventBus = koinInject<AuthEventBus>()

  val startDestination: AppRoute? by
      produceState(initialValue = null) {
        value = if (tokenRepository.getAccessToken() != null) AppRoute.Main else AppRoute.Onboarding
      }

  // Wait for the DataStore check before composing navigation.
  val destination = startDestination ?: return

  val backStack = rememberNavBackStack(destination)

  LaunchedEffect(Unit) {
    authEventBus.sessionExpired.collect {
      backStack.clear()
      backStack += AppRoute.SignIn
    }
  }

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
            entry<AppRoute.SignIn> { SignInScreen(onNavEvent = { backStack.handleNavEvent(it) }) }
            entry<AppRoute.SignUp> { SignUpScreen(onNavEvent = { backStack.handleNavEvent(it) }) }
            entry<AppRoute.Main> {
              // TODO(task 3.8): implement bottom-nav shell
              AuthPlaceholderScreen(label = "Main App")
            }
          },
  )
}

/**
 * Centralised [NavEvent] handler. Every screen entry delegates its `onNavEvent` callback here so
 * that all backstack manipulation lives in one place.
 *
 * Add new [NavEvent] cases here as features introduce them — callers need no changes.
 */
/** Placeholder until the bottom-nav shell is implemented in task 3.8. */
@Composable
private fun AuthPlaceholderScreen(label: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = label, style = MaterialTheme.typography.headlineMedium)
  }
}

private fun NavBackStack<NavKey>.handleNavEvent(event: NavEvent) {
  when (event) {
    NavEvent.GoBack -> removeLastOrNull()
    NavEvent.NavigateToMain -> {
      clear()
      this += AppRoute.Main
    }
    // Replace the current auth screen so back always returns to Onboarding,
    // never creates Sign In → Sign Up → Sign In chains.
    NavEvent.NavigateToSignIn -> {
      removeLastOrNull()
      this += AppRoute.SignIn
    }
    NavEvent.NavigateToSignUp -> {
      removeLastOrNull()
      this += AppRoute.SignUp
    }
  }
}
