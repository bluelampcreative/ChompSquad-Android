package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.EmailSignInRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.GoogleAuthRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.SignUpRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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

  /** Signs in an existing user with email and password. `POST /v1/auth/signin` */
  suspend fun signInWithEmail(email: String, password: String): Result<TokenResponseDto> =
      runCatching {
        client.post("v1/auth/signin") { setBody(EmailSignInRequestDto(email, password)) }.body()
      }

  /**
   * Registers a new user. Screen name is derived from the email local part by the caller.
   *
   * `POST /v1/auth/signup`
   */
  suspend fun signUp(
      email: String,
      password: String,
      screenName: String,
  ): Result<TokenResponseDto> = runCatching {
    client.post("v1/auth/signup") { setBody(SignUpRequestDto(email, password, screenName)) }.body()
  }
}
