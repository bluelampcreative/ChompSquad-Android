package com.bluelampcreative.chompsquad.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Sealed interface defining every top-level Navigation 3 destination in the app.
 *
 * Implementing [NavKey] is required by [androidx.navigation3.runtime.rememberNavBackStack]. Route
 * objects are plain data objects — no Parcelable or @Serializable annotations are required by
 * Navigation 3 itself (state saving uses the NavKey type system).
 */
sealed interface AppRoute : NavKey {
  // ── Auth graph ────────────────────────────────────────────────────────────

  /** Welcome / value-proposition pager. Start destination until the user authenticates. */
  data object Onboarding : AppRoute

  /** Sign-in screen — implemented in task 1.2 / 1.3. */
  data object SignIn : AppRoute

  /** Sign-up screen — implemented in task 1.3. */
  data object SignUp : AppRoute

  // ── Main graph ────────────────────────────────────────────────────────────

  /**
   * Shell for the bottom-nav tabs (Catalog / Scan / Planner / Profile). Entered once auth succeeds.
   * Implemented in task 3.8.
   */
  data object Main : AppRoute
}
