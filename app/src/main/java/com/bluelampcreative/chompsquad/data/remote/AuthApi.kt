package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.EmailSignInRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.GoogleAuthRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.LogoutRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.RefreshRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.SignUpRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Singleton

@Singleton
class AuthApi(private val client: HttpClient) {

  /**
   * Exchanges a Google ID token for a ChompSquad access + refresh token pair.
   *
   * `POST /v1/auth/google` — unauthenticated endpoint. The bearer-token auth plugin (task 1.4) is
   * intentionally absent here.
   */
  suspend fun signInWithGoogle(idToken: String): Result<TokenResponseDto> = runCatching {
    client.post("v1/auth/google") { setBody(GoogleAuthRequestDto(idToken = idToken)) }.body()
  }

  /** Signs in an existing user with email and password. `POST /v1/auth/login` */
  suspend fun signInWithEmail(email: String, password: String): Result<TokenResponseDto> =
      runCatching {
        client.post("v1/auth/login") { setBody(EmailSignInRequestDto(email, password)) }.body()
      }

  /**
   * Registers a new user. Screen name is derived from the email local part by the caller.
   *
   * `POST /v1/auth/register`
   */
  suspend fun signUp(
      email: String,
      password: String,
      screenName: String,
  ): Result<TokenResponseDto> = runCatching {
    client
        .post("v1/auth/register") { setBody(SignUpRequestDto(email, password, screenName)) }
        .body()
  }

  /**
   * Exchanges a refresh token for a new access + refresh token pair (server performs token rotation
   * — the old refresh token is invalidated). `POST /v1/auth/refresh`
   */
  suspend fun refresh(refreshToken: String): Result<TokenResponseDto> = runCatching {
    client.post("v1/auth/refresh") { setBody(RefreshRequestDto(refreshToken)) }.body()
  }

  /**
   * Invalidates the refresh token on the server (adds its JTI to the blocklist). Call before
   * clearing local tokens on sign-out. `POST /v1/auth/logout` → 204
   */
  suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
    client.post("v1/auth/logout") { setBody(LogoutRequestDto(refreshToken)) }
  }

  /**
   * Permanently deletes the authenticated user's account and all associated data. `DELETE
   * /v1/users/me` → 204
   */
  suspend fun deleteAccount(): Result<Unit> = runCatching { client.delete("v1/users/me") }
}
