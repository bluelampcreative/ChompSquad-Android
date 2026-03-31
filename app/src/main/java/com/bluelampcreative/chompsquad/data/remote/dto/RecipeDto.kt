package com.bluelampcreative.chompsquad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── API DTOs ──────────────────────────────────────────────────────────────────
// These classes map 1-to-1 with the server JSON schema.
// Do NOT use these directly in UI — map to domain models first.

@Serializable
data class RecipeListResponseDto(
    val items: List<RecipeListItemDto>,
    val total: Int,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
)

@Serializable
data class RecipeListItemDto(
    val id: String,
    @SerialName("origin_type") val originType: String,
    val title: String,
    val tags: List<String>,
    @SerialName("hero_image_url") val heroImageUrl: String?,
    @SerialName("total_time") val totalTime: Int?,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class RecipeDto(
    val id: String,
    @SerialName("origin_type") val originType: String,
    val title: String,
    @SerialName("yield_amount") val yieldAmount: String?,
    @SerialName("yield_unit") val yieldUnit: String?,
    @SerialName("prep_time") val prepTime: Int?,
    @SerialName("cook_time") val cookTime: Int?,
    @SerialName("total_time") val totalTime: Int?,
    val source: String?,
    val tags: List<String>,
    val images: List<RecipeImageDto>,
    val ingredients: List<IngredientDto>,
    val steps: List<StepDto>,
    @SerialName("personal_rating") val personalRating: Int?,
    @SerialName("personal_note") val personalNote: String?,
    @SerialName("is_favorited") val isFavorited: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class RecipeImageDto(
    val id: String,
    @SerialName("blob_path") val blobPath: String,
    val url: String,
    val position: Int,
)

@Serializable
data class IngredientDto(
    val id: String,
    val position: Int,
    val quantity: String?,
    val unit: String?,
    val name: String,
    @SerialName("prep_note") val prepNote: String?,
)

@Serializable
data class StepDto(
    val id: String,
    val position: Int,
    val instruction: String,
)

// ── Request bodies ────────────────────────────────────────────────────────────

@Serializable
data class CreateRecipeRequestDto(
    val title: String,
    @SerialName("origin_type") val originType: String,
    @SerialName("yield_amount") val yieldAmount: String? = null,
    @SerialName("yield_unit") val yieldUnit: String? = null,
    @SerialName("prep_time") val prepTime: Int? = null,
    @SerialName("cook_time") val cookTime: Int? = null,
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val ingredients: List<IngredientDto> = emptyList(),
    val steps: List<StepDto> = emptyList(),
)

@Serializable
data class PatchRecipeRequestDto(
    val title: String? = null,
    @SerialName("yield_amount") val yieldAmount: String? = null,
    @SerialName("yield_unit") val yieldUnit: String? = null,
    @SerialName("prep_time") val prepTime: Int? = null,
    @SerialName("cook_time") val cookTime: Int? = null,
    val source: String? = null,
    val tags: List<String>? = null,
    val ingredients: List<IngredientDto>? = null,
    val steps: List<StepDto>? = null,
    @SerialName("personal_rating") val personalRating: Int? = null,
    @SerialName("personal_note") val personalNote: String? = null,
    @SerialName("is_favorited") val isFavorited: Boolean? = null,
)
