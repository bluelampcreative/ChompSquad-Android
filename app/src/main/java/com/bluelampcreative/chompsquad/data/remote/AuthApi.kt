package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.GoogleAuthRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

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
}
