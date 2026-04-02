package com.bluelampcreative.chompsquad.feature.paywall

import androidx.activity.ComponentActivity
import com.bluelampcreative.chompsquad.core.ViewAction
import com.revenuecat.purchases.Package as RCPackage

enum class BillingPeriod {
  Monthly,
  Annual,
}

data class PackageUiModel(
    val rcPackage: RCPackage,
    val formattedPrice: String,
)

data class PaywallViewState(
    val isLoadingOfferings: Boolean = true,
    val monthlyPackage: PackageUiModel? = null,
    val annualPackage: PackageUiModel? = null,
    val selectedPeriod: BillingPeriod = BillingPeriod.Annual,
    val isPurchasing: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PaywallAction : ViewAction {
  data object LoadingOfferings : PaywallAction

  data class OfferingsLoaded(
      val monthly: PackageUiModel?,
      val annual: PackageUiModel?,
  ) : PaywallAction

  data class SelectPeriod(val period: BillingPeriod) : PaywallAction

  data object PurchaseStarted : PaywallAction

  /** Purchase was cancelled by the user — resets purchasing state without showing an error. */
  data object PurchaseCancelled : PaywallAction

  data class ShowError(val message: String) : PaywallAction

  data object DismissError : PaywallAction
}

sealed interface PaywallUiEvent {
  data class OnSelectPeriod(val period: BillingPeriod) : PaywallUiEvent

  data class OnPurchase(val activity: ComponentActivity) : PaywallUiEvent

  data object OnDismissError : PaywallUiEvent

  data object OnClose : PaywallUiEvent
}
