package com.bluelampcreative.chompsquad.di

import android.content.Context
import com.bluelampcreative.chompsquad.BuildConfig
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class PurchasesModule {
  @Singleton
  fun providePurchases(context: Context): Purchases {
    val key = BuildConfig.REVENUECAT_API_KEY
    require(key.isNotBlank()) {
      "REVENUECAT_API_KEY is blank — add revenuecat.api.key.android to local.properties " +
          "and re-sync the project."
    }
    Purchases.configure(PurchasesConfiguration.Builder(context = context, apiKey = key).build())
    return Purchases.sharedInstance
  }
}
