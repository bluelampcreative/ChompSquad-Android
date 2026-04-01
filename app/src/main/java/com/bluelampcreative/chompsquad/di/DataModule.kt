package com.bluelampcreative.chompsquad.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.bluelampcreative.chompsquad.data.local.AppDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("auth_tokens")

@Module
@ComponentScan("com.bluelampcreative.chompsquad.data.local")
@Configuration
class DataModule {
  @Singleton
  fun provideTokenDataStore(context: Context): DataStore<Preferences> = context.tokenDataStore

  @Singleton
  fun provideAppDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "chompsquad.db",
        )
        .build()
  }

  @Singleton fun provideRecipeDao(appDatabase: AppDatabase) = appDatabase.recipeDao()
}
