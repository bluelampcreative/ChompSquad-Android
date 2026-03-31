package com.bluelampcreative.chompsquad.di

import androidx.room.Room
import com.bluelampcreative.chompsquad.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

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
