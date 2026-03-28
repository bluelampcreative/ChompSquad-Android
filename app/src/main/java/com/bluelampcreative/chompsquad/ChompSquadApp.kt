package com.bluelampcreative.chompsquad

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ChompSquadApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ChompSquadApp)
            // Feature modules are registered here as each task is implemented.
            modules(emptyList())
        }
    }
}
