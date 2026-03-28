package com.bluelampcreative.chompsquad.di

import com.bluelampcreative.chompsquad.BuildConfig
import com.revenuecat.purchases.Purchases
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val purchasesModule = module {
    single<Purchases> {
        Purchases.configure(androidContext(), BuildConfig.REVENUECAT_API_KEY)
        Purchases.sharedInstance
    }
}
