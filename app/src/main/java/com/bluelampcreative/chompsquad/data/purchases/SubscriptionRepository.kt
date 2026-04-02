package com.bluelampcreative.chompsquad.data.purchases

import com.bluelampcreative.chompsquad.domain.model.EntitlementStatus
import kotlinx.coroutines.flow.StateFlow

/** Contract for checking and observing the user's subscription entitlement. */
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
}
