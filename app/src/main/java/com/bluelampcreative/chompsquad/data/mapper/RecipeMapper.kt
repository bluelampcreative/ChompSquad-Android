@file:Suppress(
    "TooManyFunctions"
) // Mapper files legitimately have one function per model conversion

package com.bluelampcreative.chompsquad.data.mapper

import com.bluelampcreative.chompsquad.data.local.entity.IngredientEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeImageEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeWithDetails
import com.bluelampcreative.chompsquad.data.local.entity.StepEntity
import com.bluelampcreative.chompsquad.data.remote.dto.IngredientDto
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeDto
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeImageDto
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeListItemDto
import com.bluelampcreative.chompsquad.data.remote.dto.StepDto
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.OriginType
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.domain.model.RecipeImage
import com.bluelampcreative.chompsquad.domain.model.RecipeListItem
import com.bluelampcreative.chompsquad.domain.model.Step

// ── DTO → Entity ──────────────────────────────────────────────────────────────

fun RecipeDto.toEntity(): RecipeEntity =
    RecipeEntity(
        id = id,
        originType = originType,
        title = title,
        yieldAmount = yieldAmount,
        yieldUnit = yieldUnit,
        prepTime = prepTime,
        cookTime = cookTime,
        totalTime = totalTime,
        source = source,
        // Delimiter-wrapped so SQL exact-word LIKE matching works without a join table.
        // Format: ",tag1,tag2," — see RecipeDao.observeFiltered.
        tags = if (tags.isEmpty()) "" else tags.joinToString(",", prefix = ",", postfix = ","),
        personalRating = personalRating,
        personalNote = personalNote,
        isFavorited = isFavorited,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun RecipeDto.toImageEntities(): List<RecipeImageEntity> = images.map { it.toEntity(recipeId = id) }

fun RecipeDto.toIngredientEntities(): List<IngredientEntity> =
    ingredients.map { it.toEntity(recipeId = id) }

fun RecipeDto.toStepEntities(): List<StepEntity> = steps.map { it.toEntity(recipeId = id) }

private fun RecipeImageDto.toEntity(recipeId: String) =
    RecipeImageEntity(
        id = id,
        recipeId = recipeId,
        blobPath = blobPath,
        url = url,
        position = position,
    )

private fun IngredientDto.toEntity(recipeId: String) =
    IngredientEntity(
        id = id,
        recipeId = recipeId,
        position = position,
        quantity = quantity,
        unit = unit,
        name = name,
        prepNote = prepNote,
    )

private fun StepDto.toEntity(recipeId: String) =
    StepEntity(
        id = id,
        recipeId = recipeId,
        position = position,
        instruction = instruction,
    )

// ── DTO → Domain ─────────────────────────────────────────────────────────────

fun RecipeDto.toDomain(): Recipe =
    Recipe(
        id = id,
        originType = originType.toOriginType(),
        title = title,
        yieldAmount = yieldAmount,
        yieldUnit = yieldUnit,
        prepTime = prepTime,
        cookTime = cookTime,
        totalTime = totalTime,
        source = source,
        tags = tags,
        images = images.sortedBy { it.position }.map { it.toDomainFromDto() },
        ingredients = ingredients.sortedBy { it.position }.map { it.toDomainFromDto() },
        steps = steps.sortedBy { it.position }.map { it.toDomainFromDto() },
        personalRating = personalRating,
        personalNote = personalNote,
        isFavorited = isFavorited,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

private fun RecipeImageDto.toDomainFromDto() =
    RecipeImage(id = id, blobPath = blobPath, url = url, position = position)

private fun IngredientDto.toDomainFromDto() =
    Ingredient(
        id = id,
        position = position,
        quantity = quantity,
        unit = unit,
        name = name,
        prepNote = prepNote,
    )

private fun StepDto.toDomainFromDto() =
    Step(id = id, position = position, instruction = instruction)

// ── Entity → Domain ───────────────────────────────────────────────────────────

fun RecipeWithDetails.toDomain(): Recipe =
    Recipe(
        id = recipe.id,
        originType = recipe.originType.toOriginType(),
        title = recipe.title,
        yieldAmount = recipe.yieldAmount,
        yieldUnit = recipe.yieldUnit,
        prepTime = recipe.prepTime,
        cookTime = recipe.cookTime,
        totalTime = recipe.totalTime,
        source = recipe.source,
        tags = recipe.tags.split(",").filter { it.isNotBlank() },
        images = images.sortedBy { it.position }.map { it.toDomain() },
        ingredients = ingredients.sortedBy { it.position }.map { it.toDomain() },
        steps = steps.sortedBy { it.position }.map { it.toDomain() },
        personalRating = recipe.personalRating,
        personalNote = recipe.personalNote,
        isFavorited = recipe.isFavorited,
        createdAt = recipe.createdAt,
        updatedAt = recipe.updatedAt,
    )

private fun RecipeImageEntity.toDomain() =
    RecipeImage(
        id = id,
        blobPath = blobPath,
        url = url,
        position = position,
    )

private fun IngredientEntity.toDomain() =
    Ingredient(
        id = id,
        position = position,
        quantity = quantity,
        unit = unit,
        name = name,
        prepNote = prepNote,
    )

private fun StepEntity.toDomain() =
    Step(
        id = id,
        position = position,
        instruction = instruction,
    )

// ── RecipeWithDetails → RecipeListItem ────────────────────────────────────────

fun RecipeWithDetails.toListItem(): RecipeListItem {
  val hero = images.minByOrNull { it.position }
  return RecipeListItem(
      id = recipe.id,
      originType = recipe.originType.toOriginType(),
      title = recipe.title,
      tags = recipe.tags.split(",").filter { it.isNotBlank() },
      heroImageUrl = hero?.url,
      heroImageId = hero?.id,
      heroBlobPath = hero?.blobPath,
      totalTime = recipe.totalTime,
      createdAt = recipe.createdAt,
  )
}

// ── RecipeListItemDto → Domain / Entity ───────────────────────────────────────

fun RecipeListItemDto.toDomain(): RecipeListItem =
    RecipeListItem(
        id = id,
        originType = originType.toOriginType(),
        title = title,
        tags = tags,
        heroImageUrl = heroImageUrl,
        heroImageId = null, // list endpoint does not return image IDs
        heroBlobPath = null, // list endpoint does not return blob paths
        totalTime = totalTime,
        createdAt = createdAt,
    )

/**
 * Builds a minimal [RecipeEntity] from a list item for `INSERT OR IGNORE` caching. Missing fields
 * default to null/false so that any existing full recipe stored by a previous save is never
 * downgraded. [upsertRecipeWithDetails] remains the authoritative write path for full recipes.
 */
fun RecipeListItemDto.toMinimalEntity(): RecipeEntity =
    RecipeEntity(
        id = id,
        originType = originType,
        title = title,
        yieldAmount = null,
        yieldUnit = null,
        prepTime = null,
        cookTime = null,
        totalTime = totalTime,
        source = null,
        tags = if (tags.isEmpty()) "" else tags.joinToString(",", prefix = ",", postfix = ","),
        personalRating = null,
        personalNote = null,
        isFavorited = false,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

/**
 * Builds a stub [RecipeImageEntity] for the hero image URL received from the list endpoint. Uses a
 * deterministic ID so the temporary hero stub is predictable while cached from list data; when a
 * later [upsertRecipeWithDetails] call stores full recipe details, it deletes existing image rows
 * for the recipe and inserts the real detail images. [blobPath] is left blank — image refresh is
 * only possible after the full detail has been fetched and the real blob path is stored.
 */
fun RecipeListItemDto.toHeroImageEntity(): RecipeImageEntity? =
    heroImageUrl?.let { url ->
      RecipeImageEntity(
          id = "${id}_hero_stub",
          recipeId = id,
          blobPath = "",
          url = url,
          position = 0,
      )
    }

// ── Helpers ───────────────────────────────────────────────────────────────────

fun String.toOriginType(): OriginType =
    when (this) {
      "scanned" -> OriginType.SCANNED
      "manual" -> OriginType.MANUAL
      "social_save" -> OriginType.SOCIAL_SAVE
      else -> OriginType.UNKNOWN // Explicit — do not silently misclassify
    }
