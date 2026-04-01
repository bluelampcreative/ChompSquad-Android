package com.bluelampcreative.chompsquad.data.purchases

import com.bluelampcreative.chompsquad.BuildConfig
import com.bluelampcreative.chompsquad.domain.model.EntitlementStatus
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton

private const val PRO_ENTITLEMENT_ID = "pro"

@Singleton(binds = [SubscriptionRepository::class])
class RevenueCatSubscriptionRepository(private val purchases: Purchases) : SubscriptionRepository {

  // App-scoped scope — lives as long as the singleton (i.e. the process lifetime).
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  // Initialise to true for DEBUG builds so developer flows are never gated during development.
  private val _entitlementStatus = MutableStateFlow(EntitlementStatus(hasPro = BuildConfig.DEBUG))
  override val entitlementStatus: StateFlow<EntitlementStatus> = _entitlementStatus.asStateFlow()

  init {
    // Kick off a background refresh immediately so entitlement status is current by the time
    // any screen collects it.
    scope.launch { refreshEntitlements() }
  }

  override suspend fun refreshEntitlements() {
    // Developer bypass — always grant pro in debug builds, skip the network call.
    if (BuildConfig.DEBUG) {
      _entitlementStatus.value = EntitlementStatus(hasPro = true)
      return
    }

    runCatching {
          suspendCancellableCoroutine { cont ->
            purchases.getCustomerInfo(
                object : ReceiveCustomerInfoCallback {
                  override fun onReceived(customerInfo: CustomerInfo) {
                    cont.resume(customerInfo)
                  }

                  override fun onError(error: PurchasesError) {
                    cont.resumeWithException(Exception(error.message))
                  }
                }
            )
          }
        }
        .onSuccess { customerInfo ->
          val hasPro = customerInfo.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
          _entitlementStatus.value = EntitlementStatus(hasPro = hasPro)
        }
    // On failure, retain the last-known status. Transient network errors should not downgrade
    // a user who was previously confirmed as pro.
  }
}
