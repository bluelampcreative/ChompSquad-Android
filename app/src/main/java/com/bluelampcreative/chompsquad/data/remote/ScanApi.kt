package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Singleton

@Singleton
class ScanApi(private val client: HttpClient) {

  /**
   * Uploads [pages] as a multipart form POST to `v1/scan`. Each page is a JPEG [ByteArray] produced
   * by [com.bluelampcreative.chompsquad.data.scanner.ImagePreprocessor]. Returns the
   * server-extracted [RecipeDto] on success.
   */
  suspend fun submitScan(pages: List<ByteArray>): Result<RecipeDto> = runCatching {
    client
        .submitFormWithBinaryData(
            url = "v1/scan",
            formData =
                formData {
                  pages.forEachIndexed { index, bytes ->
                    append(
                        key = "pages",
                        value = bytes,
                        headers =
                            Headers.build {
                              append(HttpHeaders.ContentType, "image/jpeg")
                              append(
                                  HttpHeaders.ContentDisposition,
                                  "form-data; name=\"pages\"; filename=\"page_${index + 1}.jpg\"",
                              )
                            },
                    )
                  }
                },
        )
        .body()
  }
}
