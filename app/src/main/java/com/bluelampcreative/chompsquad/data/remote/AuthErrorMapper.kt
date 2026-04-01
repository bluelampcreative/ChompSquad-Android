package com.bluelampcreative.chompsquad.data.remote

import android.util.Log
import io.ktor.client.plugins.ResponseException
import java.io.IOException

private const val TAG = "AuthError"

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_CONFLICT = 409
private const val HTTP_CLIENT_ERROR_MIN = 400
private const val HTTP_CLIENT_ERROR_MAX = 499
private const val HTTP_SERVER_ERROR_MIN = 500
private const val HTTP_SERVER_ERROR_MAX = 599

/**
 * Maps a [Throwable] from an auth API call to a user-friendly error message, while logging the
 * original exception at error level so it remains visible in crash tooling.
 *
 * HTTP status codes are mapped to specific messages; network I/O errors surface a connectivity
 * hint; everything else falls back to [fallback] so no raw technical strings reach the UI.
 */
internal fun Throwable.toAuthErrorMessage(fallback: String): String {
  Log.e(TAG, "Auth request failed", this)
  return when (this) {
    is ResponseException ->
        when (response.status.value) {
          HTTP_UNAUTHORIZED -> "Incorrect email or password."
          HTTP_FORBIDDEN -> "Access denied. Please try again."
          HTTP_CONFLICT -> "An account with this email already exists. Try signing in."
          in HTTP_CLIENT_ERROR_MIN..HTTP_CLIENT_ERROR_MAX ->
              "Please check your details and try again."
          in HTTP_SERVER_ERROR_MIN..HTTP_SERVER_ERROR_MAX ->
              "Something went wrong on our end. Please try again later."
          else -> fallback
        }
    is IOException -> "Check your connection and try again."
    else -> fallback
  }
}
