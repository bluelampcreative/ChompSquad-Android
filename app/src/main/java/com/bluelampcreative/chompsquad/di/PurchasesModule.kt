package com.bluelampcreative.chompsquad.di

import android.content.Context
import com.bluelampcreative.chompsquad.BuildConfig
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.bluelampcreative.chompsquad.data.purchases")
@Configuration
class PurchasesModule {
  @Singleton
  fun providePurchases(context: Context): Purchases {
    val key = BuildConfig.REVENUECAT_API_KEY

    check(key.isNotBlank() || BuildConfig.DEBUG) {
      "REVENUECAT_API_KEY is blank — add revenuecat.api.key.android to local.properties " +
          "and re-sync the project."
    }

    // In debug builds without a real key, configure with a placeholder. The
    // RevenueCatSubscriptionRepository bypasses all SDK calls in debug mode, so this key
    // is never used to make a real network request.
    val configKey = key.ifBlank { "debug_placeholder_key" }
    Purchases.configure(
        PurchasesConfiguration.Builder(context = context, apiKey = configKey).build()
    )
    return Purchases.sharedInstance
  }
}
