package com.bluelampcreative.chompsquad.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// ── Room entities ─────────────────────────────────────────────────────────────

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "origin_type")    val originType: String,
    val title: String,
    @ColumnInfo(name = "yield_amount")   val yieldAmount: String?,
    @ColumnInfo(name = "yield_unit")     val yieldUnit: String?,
    @ColumnInfo(name = "prep_time")      val prepTime: Int?,
    @ColumnInfo(name = "cook_time")      val cookTime: Int?,
    @ColumnInfo(name = "total_time")     val totalTime: Int?,
    val source: String?,
    // Tags stored as a comma-separated string; split on read via TypeConverter.
    val tags: String,
    @ColumnInfo(name = "personal_rating") val personalRating: Int?,
    @ColumnInfo(name = "personal_note")   val personalNote: String?,
    @ColumnInfo(name = "is_favorited")    val isFavorited: Boolean,
    @ColumnInfo(name = "created_at")      val createdAt: String,
    @ColumnInfo(name = "updated_at")      val updatedAt: String,
)

@Entity(
    tableName = "recipe_images",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipe_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipe_id")],
)
data class RecipeImageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "blob_path") val blobPath: String,
    val url: String,
    val position: Int,
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipe_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipe_id")],
)
data class IngredientEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val position: Int,
    val quantity: String?,
    val unit: String?,
    val name: String,
    @ColumnInfo(name = "prep_note") val prepNote: String?,
)

@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipe_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipe_id")],
)
data class StepEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val position: Int,
    val instruction: String,
)

// ── Relation aggregate ────────────────────────────────────────────────────────
// Used by Room to load a recipe with all its child rows in one query.

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    val images: List<RecipeImageEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    val ingredients: List<IngredientEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    val steps: List<StepEntity>,
)
