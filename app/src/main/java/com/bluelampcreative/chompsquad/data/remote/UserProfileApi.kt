package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.FeedbackRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Singleton

@Singleton
class UserProfileApi(private val client: HttpClient) {

  suspend fun getProfile(): Result<UserProfileDto> = runCatching {
    client.get("v1/users/me").body()
  }

  suspend fun uploadAvatar(imageBytes: ByteArray, mimeType: String): Result<Unit> = runCatching {
    client.put("v1/users/me/avatar") {
      setBody(
          MultiPartFormDataContent(
              formData {
                append(
                    "avatar",
                    imageBytes,
                    Headers.build {
                      append(HttpHeaders.ContentType, mimeType)
                      append(HttpHeaders.ContentDisposition, "filename=\"avatar\"")
                    },
                )
              }
          )
      )
    }
  }

  suspend fun submitFeedback(message: String): Result<Unit> = runCatching {
    client.post("v1/feedback") { setBody(FeedbackRequestDto(message)) }
  }
}
