package com.bluelampcreative.chompsquad.domain.model

/**
 * Represents the user's current subscription entitlement state.
 *
 * [hasPro] is true when the user has an active Pro entitlement in RevenueCat, or when the
 * developer-tier bypass is in effect ([BuildConfig.DEBUG] or `subscription_tier == "developer"`).
 */
data class EntitlementStatus(val hasPro: Boolean)
