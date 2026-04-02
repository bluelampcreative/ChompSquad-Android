package com.bluelampcreative.chompsquad.feature.paywall

import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.purchases.PurchaseException
import com.bluelampcreative.chompsquad.data.purchases.SubscriptionRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PaywallViewModel(
    private val subscriptionRepository: SubscriptionRepository,
) : CoreViewModel<PaywallViewState, PaywallAction, PaywallUiEvent>(PaywallViewState()) {

  init {
    loadOfferings()
  }

  override fun reducer(state: PaywallViewState, action: PaywallAction): PaywallViewState =
      when (action) {
        is PaywallAction.LoadingOfferings -> state.copy(isLoadingOfferings = true)
        is PaywallAction.OfferingsLoaded ->
            state.copy(
                isLoadingOfferings = false,
                monthlyPackage = action.monthly,
                annualPackage = action.annual,
            )
        is PaywallAction.SelectPeriod -> state.copy(selectedPeriod = action.period)
        is PaywallAction.PurchaseStarted -> state.copy(isPurchasing = true, errorMessage = null)
        is PaywallAction.ShowError ->
            state.copy(isPurchasing = false, errorMessage = action.message)
        is PaywallAction.DismissError -> state.copy(errorMessage = null)
      }

  override fun handleEvent(event: PaywallUiEvent) {
    when (event) {
      is PaywallUiEvent.OnSelectPeriod -> state.dispatch(PaywallAction.SelectPeriod(event.period))
      is PaywallUiEvent.OnPurchase -> purchase(event.activity)
      PaywallUiEvent.OnDismissError -> state.dispatch(PaywallAction.DismissError)
      PaywallUiEvent.OnClose -> navigate(NavEvent.GoBack)
    }
  }

  private fun loadOfferings() {
    viewModelScope.launch {
      state.dispatch(PaywallAction.LoadingOfferings)
      val current = subscriptionRepository.getOfferings()?.current
      state.dispatch(
          PaywallAction.OfferingsLoaded(
              monthly = current?.monthly?.let { PackageUiModel(it, it.product.price.formatted) },
              annual = current?.annual?.let { PackageUiModel(it, it.product.price.formatted) },
          )
      )
    }
  }

  private fun purchase(activity: ComponentActivity) {
    val currentState = state.value
    val selectedPackage =
        when (currentState.selectedPeriod) {
          BillingPeriod.Monthly -> currentState.monthlyPackage?.rcPackage
          BillingPeriod.Annual -> currentState.annualPackage?.rcPackage
        } ?: return

    viewModelScope.launch {
      state.dispatch(PaywallAction.PurchaseStarted)
      subscriptionRepository
          .purchase(activity, selectedPackage)
          .onSuccess {
            subscriptionRepository.refreshEntitlements()
            navigate(NavEvent.GoBack)
          }
          .onFailure { error ->
            // Silently dismiss if the user cancelled — no error toast needed.
            if (error is PurchaseException && error.userCancelled) {
              state.dispatch(PaywallAction.DismissError)
            } else {
              state.dispatch(
                  PaywallAction.ShowError(error.message ?: "Purchase failed. Please try again.")
              )
            }
          }
    }
  }
}
