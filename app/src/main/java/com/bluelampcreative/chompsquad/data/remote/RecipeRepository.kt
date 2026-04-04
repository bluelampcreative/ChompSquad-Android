package com.bluelampcreative.chompsquad.data.remote

import com.bluelampcreative.chompsquad.data.local.dao.RecipeDao
import com.bluelampcreative.chompsquad.data.mapper.toDomain
import com.bluelampcreative.chompsquad.data.mapper.toEntity
import com.bluelampcreative.chompsquad.data.mapper.toImageEntities
import com.bluelampcreative.chompsquad.data.mapper.toIngredientEntities
import com.bluelampcreative.chompsquad.data.mapper.toStepEntities
import com.bluelampcreative.chompsquad.data.remote.dto.CreateRecipeRequestDto
import com.bluelampcreative.chompsquad.domain.model.Recipe
import org.koin.core.annotation.Singleton

interface RecipeRepository {
  /**
   * POSTs [request] to `v1/recipes`, writes the server response through to the local Room database,
   * and returns the persisted [Recipe] domain model.
   */
  suspend fun saveRecipe(request: CreateRecipeRequestDto): Result<Recipe>
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
}
