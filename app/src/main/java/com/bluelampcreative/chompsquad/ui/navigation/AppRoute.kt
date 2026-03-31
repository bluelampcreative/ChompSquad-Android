package com.bluelampcreative.chompsquad.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Sealed interface defining every top-level Navigation 3 destination in the app.
 *
 * Each concrete route must be annotated with [@Serializable] — Navigation 3 rc01 uses
 * kotlinx.serialization to persist the backstack across configuration changes. [NavKey] is the
 * marker interface required by [androidx.navigation3.runtime.rememberNavBackStack].
 */
sealed interface AppRoute : NavKey {
  // ── Auth graph ────────────────────────────────────────────────────────────

  /** Welcome / value-proposition pager. Start destination until the user authenticates. */
  @Serializable data object Onboarding : AppRoute

  /** Sign-in screen — implemented in task 1.2 / 1.3. */
  @Serializable data object SignIn : AppRoute

  /** Sign-up screen — implemented in task 1.3. */
  @Serializable data object SignUp : AppRoute

  // ── Main graph ────────────────────────────────────────────────────────────

  /**
   * Shell for the bottom-nav tabs (Catalog / Scan / Planner / Profile). Entered once auth succeeds.
   * Implemented in task 3.8.
   */
  @Serializable data object Main : AppRoute
}
