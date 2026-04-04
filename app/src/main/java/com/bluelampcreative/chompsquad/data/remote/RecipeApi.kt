package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.remote.dto.CreateRecipeRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeDto
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeListResponseDto
import com.bluelampcreative.chompsquad.data.remote.dto.RefreshImageUrlRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.RefreshImageUrlResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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

  /** Fetches a paginated recipe list. [tag] and [search] are optional server-side filters. */
  suspend fun getRecipes(
      page: Int = 1,
      pageSize: Int = 20,
      tag: String? = null,
      search: String? = null,
  ): Result<RecipeListResponseDto> = runCatching {
    client
        .get("v1/recipes") {
          parameter("page", page)
          parameter("page_size", pageSize)
          if (tag != null) parameter("tag", tag)
          if (!search.isNullOrBlank()) parameter("search", search)
        }
        .body()
  }

  /** Fetches the full detail for a single recipe by [id]. */
  suspend fun getRecipeById(id: String): Result<RecipeDto> = runCatching {
    client.get("v1/recipes/$id").body()
  }

  /**
   * Requests a fresh signed URL for an image identified by its [blobPath]. Call when a load attempt
   * returns 403 — signed URLs expire after a server-configured TTL.
   */
  suspend fun refreshImageUrl(blobPath: String): Result<String> = runCatching {
    client
        .post("v1/images/refresh-url") { setBody(RefreshImageUrlRequestDto(blobPath)) }
        .body<RefreshImageUrlResponseDto>()
        .url
  }
}
