package com.bluelampcreative.chompsquad.di

import com.bluelampcreative.chompsquad.BuildConfig
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val purchasesModule = module {
  single<Purchases> {
    val key = BuildConfig.REVENUECAT_API_KEY
    require(key.isNotBlank()) {
      "REVENUECAT_API_KEY is blank — add revenuecat.api.key.android to local.properties " +
          "and re-sync the project."
    }
    Purchases.configure(
        PurchasesConfiguration.Builder(context = androidContext(), apiKey = key).build()
    )
    Purchases.sharedInstance
  }
}
