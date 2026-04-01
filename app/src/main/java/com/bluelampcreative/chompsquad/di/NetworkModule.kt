package com.bluelampcreative.chompsquad.di

import android.util.Log
import com.bluelampcreative.chompsquad.BuildConfig
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.remote.AuthEventBus
import com.bluelampcreative.chompsquad.data.remote.dto.RefreshRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module(includes = [DataModule::class])
@ComponentScan("com.bluelampcreative.chompsquad.data.remote")
@Configuration
class NetworkModule {
  @Singleton
  fun provideHttpClient(tokenRepository: TokenRepository, authEventBus: AuthEventBus): HttpClient {
    return HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
            Json {
              ignoreUnknownKeys = true
              isLenient = true
            }
        )
      }
      install(Logging) {
        logger =
            object : Logger {
              override fun log(message: String) {
                Log.d("KtorHttpClient", message)
              }
            }
        level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
      }
      install(Auth) {
        bearer {
          loadTokens {
            val access = tokenRepository.getAccessToken() ?: return@loadTokens null
            val refresh = tokenRepository.getRefreshToken() ?: return@loadTokens null
            BearerTokens(access, refresh)
          }
          refreshTokens {
            // oldTokens is null when loadTokens returned null (user not logged in).
            // Return null so the 401 propagates — don't treat this as a session expiry.
            val oldRefresh = oldTokens?.refreshToken ?: return@refreshTokens null

            runCatching {
                  client
                      .post("v1/auth/refresh") {
                        markAsRefreshTokenRequest()
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequestDto(oldRefresh))
                      }
                      .body<TokenResponseDto>()
                }
                .getOrNull()
                ?.let { tokens ->
                  tokenRepository.saveTokens(tokens.accessToken, tokens.refreshToken)
                  BearerTokens(tokens.accessToken, tokens.refreshToken)
                }
                ?: run {
                  tokenRepository.clearTokens()
                  authEventBus.emitSessionExpired()
                  null
                }
          }
          // Auth endpoints are self-authenticating (credentials in body) — no bearer header.
          sendWithoutRequest { request -> !request.url.toString().contains("auth/") }
        }
      }
      expectSuccess = true
      defaultRequest {
        url(BuildConfig.API_BASE_URL)
        contentType(ContentType.Application.Json)
      }
    }
  }
}
