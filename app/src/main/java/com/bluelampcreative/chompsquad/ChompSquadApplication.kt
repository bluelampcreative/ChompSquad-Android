package com.bluelampcreative.chompsquad

import android.app.Application
import com.bluelampcreative.chompsquad.di.DataModule
import com.bluelampcreative.chompsquad.di.NetworkModule
import com.bluelampcreative.chompsquad.di.PurchasesModule
import com.bluelampcreative.chompsquad.di.ViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinApplication
import org.koin.core.logger.Level
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(
    modules =
        [
            ViewModelModule::class,
            DataModule::class,
            NetworkModule::class,
            PurchasesModule::class,
        ]
)
private object KoinConfig

class ChompSquadApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin<KoinConfig> {
      androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
      androidContext(this@ChompSquadApplication)
    }
  }
}
