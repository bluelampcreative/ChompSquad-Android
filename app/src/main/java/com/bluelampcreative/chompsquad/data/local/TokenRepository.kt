package com.bluelampcreative.chompsquad.data.local

/**
 * Contract for persisting the JWT access and refresh token pair.
 *
 * Task 1.4 provides the production implementation backed by Preferences DataStore + Android
 * Keystore. [InMemoryTokenRepository] is a lightweight stub used until that wiring is complete.
 */
interface TokenRepository {
  suspend fun saveTokens(accessToken: String, refreshToken: String)

  suspend fun getAccessToken(): String?

  suspend fun getRefreshToken(): String?

  suspend fun clearTokens()
}

/**
 * In-memory stub — tokens are lost on process death. Replaced by the DataStore-backed
 * implementation in task 1.4.
 */
class InMemoryTokenRepository : TokenRepository {
  private var accessToken: String? = null
  private var refreshToken: String? = null

  override suspend fun saveTokens(accessToken: String, refreshToken: String) {
    this.accessToken = accessToken
    this.refreshToken = refreshToken
  }

  override suspend fun getAccessToken(): String? = accessToken

  override suspend fun getRefreshToken(): String? = refreshToken

  override suspend fun clearTokens() {
    accessToken = null
    refreshToken = null
  }
}
