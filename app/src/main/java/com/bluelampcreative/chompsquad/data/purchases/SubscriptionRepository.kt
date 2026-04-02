package com.bluelampcreative.chompsquad.data.purchases

import androidx.activity.ComponentActivity
import com.bluelampcreative.chompsquad.domain.model.EntitlementStatus
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package as RCPackage
import kotlinx.coroutines.flow.StateFlow

/** Contract for checking, observing, and purchasing subscription entitlements. */
interface SubscriptionRepository {
  /**
   * Current entitlement status as a [StateFlow]. Emits immediately with the last-known value and
   * updates after each [refreshEntitlements] call or purchase event.
   */
  val entitlementStatus: StateFlow<EntitlementStatus>

  /**
   * Fetches the latest customer info from RevenueCat and updates [entitlementStatus]. Safe to call
   * from any coroutine context. On transient failure the current status is retained so a confirmed
   * pro user is not inadvertently downgraded.
   */
  suspend fun refreshEntitlements()

  /**
   * Fetches the current RevenueCat [Offerings]. Returns null on failure (e.g. no network). Callers
   * should gracefully handle a null result.
   */
  suspend fun getOfferings(): Offerings?

  /**
   * Initiates a Google Play Billing purchase for [packageItem] via RevenueCat. Returns a [Result]
   * wrapping the resulting [CustomerInfo] on success, or the error on failure.
   *
   * Must be called from a UI context — [activity] is required by the Play Billing flow.
   */
  suspend fun purchase(activity: ComponentActivity, packageItem: RCPackage): Result<CustomerInfo>

  /**
   * Restores previous Google Play purchases for the current user via RevenueCat and updates
   * [entitlementStatus]. Returns a [Result] wrapping [CustomerInfo] on success.
   */
  /**
   * Restores previous Google Play purchases for the current user via RevenueCat and updates
   * [entitlementStatus]. Returns [Result.success] on success (including the debug-bypass path).
   */
  suspend fun restorePurchases(): Result<Unit>
}
