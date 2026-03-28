package com.bluelampcreative.chompsquad

import android.app.Application
import com.bluelampcreative.chompsquad.di.dataModule
import com.bluelampcreative.chompsquad.di.purchasesModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ChompSquadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@ChompSquadApplication)
            modules(
                dataModule,
                purchasesModule,
            )
        }
    }
}
