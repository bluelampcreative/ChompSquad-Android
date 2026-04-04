package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.local.dao.RecipeDao
import com.bluelampcreative.chompsquad.data.mapper.toDomain
import com.bluelampcreative.chompsquad.data.mapper.toEntity
import com.bluelampcreative.chompsquad.data.mapper.toHeroImageEntity
import com.bluelampcreative.chompsquad.data.mapper.toImageEntities
import com.bluelampcreative.chompsquad.data.mapper.toIngredientEntities
import com.bluelampcreative.chompsquad.data.mapper.toListItem
import com.bluelampcreative.chompsquad.data.mapper.toMinimalEntity
import com.bluelampcreative.chompsquad.data.mapper.toStepEntities
import com.bluelampcreative.chompsquad.data.remote.dto.CreateRecipeRequestDto
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.domain.model.RecipeListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton

interface RecipeRepository {
  /**
   * POSTs [request] to `v1/recipes`, writes the server response through to the local Room database,
   * and returns the persisted [Recipe] domain model.
   */
  suspend fun saveRecipe(request: CreateRecipeRequestDto): Result<Recipe>

  /**
   * Emits the locally cached recipe list, re-emitting whenever the database changes. Applies [tag]
   * and [search] filters in SQL — both are optional.
   */
  fun observeRecipes(tag: String?, search: String?): Flow<List<RecipeListItem>>

  /**
   * Fetches page [page] from the server with optional [tag] / [search] filters, then stubs any
   * unknown items into Room via `INSERT OR IGNORE` so they appear in [observeRecipes]. Items
   * already stored as full recipes are never downgraded.
   */
  suspend fun syncRecipes(
      page: Int = 1,
      pageSize: Int = 100,
      tag: String? = null,
      search: String? = null,
  ): Result<Unit>

  /**
   * Calls `POST /v1/images/refresh-url` to obtain a fresh signed URL for [blobPath], then updates
   * the stored URL in Room so [observeRecipes] re-emits with the corrected URL. Only call when
   * [blobPath] is non-blank (full recipes have real blob paths; list stubs do not).
   */
  suspend fun refreshHeroImageUrl(imageId: String, blobPath: String): Result<Unit>
}

@Singleton(binds = [RecipeRepository::class])
class DefaultRecipeRepository(
    private val recipeApi: RecipeApi,
    private val recipeDao: RecipeDao,
) : RecipeRepository {

  override suspend fun saveRecipe(request: CreateRecipeRequestDto): Result<Recipe> =
      recipeApi.createRecipe(request).mapCatching { recipeDto ->
        recipeDao.upsertRecipeWithDetails(
            recipe = recipeDto.toEntity(),
            images = recipeDto.toImageEntities(),
            ingredients = recipeDto.toIngredientEntities(),
            steps = recipeDto.toStepEntities(),
        )
        recipeDto.toDomain()
      }

  override fun observeRecipes(tag: String?, search: String?): Flow<List<RecipeListItem>> =
      recipeDao.observeFiltered(tag, search).map { list -> list.map { it.toListItem() } }

  override suspend fun syncRecipes(
      page: Int,
      pageSize: Int,
      tag: String?,
      search: String?,
  ): Result<Unit> =
      recipeApi.getRecipes(page, pageSize, tag, search).mapCatching { response ->
        val recipes = response.items.map { it.toMinimalEntity() }
        val images = response.items.mapNotNull { it.toHeroImageEntity() }
        recipeDao.insertListStubs(recipes, images)
      }

  override suspend fun refreshHeroImageUrl(imageId: String, blobPath: String): Result<Unit> =
      recipeApi.refreshImageUrl(blobPath).mapCatching { newUrl ->
        recipeDao.refreshImageUrl(imageId, newUrl)
      }
}
