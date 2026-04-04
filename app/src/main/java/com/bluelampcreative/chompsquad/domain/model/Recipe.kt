package com.bluelampcreative.chompsquad.domain.model

// ── Domain models ─────────────────────────────────────────────────────────────
// These are the canonical in-app representations used by UI and business logic.
// They are decoupled from both the API DTO schema and the Room entity schema.

enum class OriginType {
  SCANNED,
  MANUAL,
  SOCIAL_SAVE,
  /** Returned by the server for a type this client does not yet recognise. */
  UNKNOWN,
}

data class Recipe(
    val id: String,
    val originType: OriginType,
    val title: String,
    val yieldAmount: String?,
    val yieldUnit: String?,
    val prepTime: Int?,
    val cookTime: Int?,
    val totalTime: Int?,
    val source: String?,
    val tags: List<String>,
    val images: List<RecipeImage>,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val personalRating: Int?,
    val personalNote: String?,
    val isFavorited: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

data class RecipeListItem(
    val id: String,
    val originType: OriginType,
    val title: String,
    val tags: List<String>,
    val heroImageUrl: String?,
    /**
     * Room image ID for the hero image. Used to update the URL in Room after a 403 refresh. Null
     * for list-only items that haven't had their detail fetched yet.
     */
    val heroImageId: String?,
    /**
     * Blob path for the hero image. Required to call `POST /v1/images/refresh-url`. Blank for
     * list-only stubs where the detail has not been fetched yet — check before calling refresh.
     */
    val heroBlobPath: String?,
    val totalTime: Int?,
    val createdAt: String,
)

data class RecipeImage(
    val id: String,
    val blobPath: String,
    val url: String,
    val position: Int,
)

data class Ingredient(
    val id: String,
    val position: Int,
    val quantity: String?,
    val unit: String?,
    val name: String,
    val prepNote: String?,
)

data class Step(
    val id: String,
    val position: Int,
    val instruction: String,
)
