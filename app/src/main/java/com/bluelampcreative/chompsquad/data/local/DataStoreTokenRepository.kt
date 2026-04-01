package com.bluelampcreative.chompsquad.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton

@Singleton(binds = [TokenRepository::class])
class DataStoreTokenRepository(private val dataStore: DataStore<Preferences>) : TokenRepository {

  override suspend fun saveTokens(accessToken: String, refreshToken: String) {
    dataStore.edit { prefs ->
      prefs[Keys.ACCESS_TOKEN] = accessToken
      prefs[Keys.REFRESH_TOKEN] = refreshToken
    }
  }

  override suspend fun getAccessToken(): String? =
      dataStore.data.catch { emit(emptyPreferences()) }.map { it[Keys.ACCESS_TOKEN] }.firstOrNull()

  override suspend fun getRefreshToken(): String? =
      dataStore.data
          .catch { emit(emptyPreferences()) }
          .map { it[Keys.REFRESH_TOKEN] }
          .firstOrNull()

  override suspend fun hasValidSession(): Boolean =
      getAccessToken() != null && getRefreshToken() != null

  override suspend fun clearTokens() {
    dataStore.edit { prefs ->
      prefs.remove(Keys.ACCESS_TOKEN)
      prefs.remove(Keys.REFRESH_TOKEN)
    }
  }

  private object Keys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
  }
}
