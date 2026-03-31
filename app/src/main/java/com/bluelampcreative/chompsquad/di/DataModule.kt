package com.bluelampcreative.chompsquad.di

import androidx.room.Room
import com.bluelampcreative.chompsquad.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.dsl.module

@Module @ComponentScan("com.bluelampcreative.chompsquad.data") @Configuration class DataModule

val dataModule = module {
  single<AppDatabase> {
    Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "chompsquad.db",
        )
        .build()
  }

  single { get<AppDatabase>().recipeDao() }
}
