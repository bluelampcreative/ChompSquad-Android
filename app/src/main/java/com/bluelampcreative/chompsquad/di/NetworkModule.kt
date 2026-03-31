package com.bluelampcreative.chompsquad.di

import android.util.Log
import com.bluelampcreative.chompsquad.BuildConfig
import com.bluelampcreative.chompsquad.data.local.InMemoryTokenRepository
import com.bluelampcreative.chompsquad.data.local.TokenRepository
import com.bluelampcreative.chompsquad.data.remote.AuthApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
  single<HttpClient> {
    HttpClient(CIO) {
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
      defaultRequest {
        url(BuildConfig.API_BASE_URL)
        contentType(ContentType.Application.Json)
      }
    }
  }

  single { AuthApi(get()) }

  // In-memory stub until task 1.4 wires the DataStore-backed implementation.
  single<TokenRepository> { InMemoryTokenRepository() }
}
