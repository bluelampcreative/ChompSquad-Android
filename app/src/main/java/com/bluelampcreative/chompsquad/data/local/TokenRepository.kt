package com.bluelampcreative.chompsquad.data.local

/** Contract for persisting the JWT access and refresh token pair. */
interface TokenRepository {
  suspend fun saveTokens(accessToken: String, refreshToken: String)

  suspend fun getAccessToken(): String?

  suspend fun getRefreshToken(): String?

  suspend fun clearTokens()

  /** Returns true only when both access and refresh tokens are present. */
  suspend fun hasValidSession(): Boolean
}
