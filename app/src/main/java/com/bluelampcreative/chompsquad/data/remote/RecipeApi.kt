package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.CreateRecipeRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Singleton

@Singleton
class RecipeApi(private val client: HttpClient) {

  /**
   * POSTs a new recipe to `v1/recipes`. Returns the server-created [RecipeDto] (with a stable ID)
   * on success.
   */
  suspend fun createRecipe(request: CreateRecipeRequestDto): Result<RecipeDto> = runCatching {
    client.post("v1/recipes") { setBody(request) }.body()
  }
}
