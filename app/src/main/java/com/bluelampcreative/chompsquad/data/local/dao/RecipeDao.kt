package com.bluelampcreative.chompsquad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bluelampcreative.chompsquad.data.local.entity.IngredientEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeImageEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeWithDetails
import com.bluelampcreative.chompsquad.data.local.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY created_at DESC")
    fun observeAll(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE is_favorited = 1 ORDER BY created_at DESC")
    fun observeFavorites(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeById(id: String): Flow<RecipeWithDetails?>

    @Transaction
    @Query("""
        SELECT * FROM recipes
        WHERE (:tag IS NULL OR tags LIKE '%' || :tag || '%')
        AND (:search IS NULL OR title LIKE '%' || :search || '%')
        ORDER BY created_at DESC
    """)
    fun observeFiltered(tag: String?, search: String?): Flow<List<RecipeWithDetails>>

    // ── Writes ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImages(images: List<RecipeImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIngredients(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSteps(steps: List<StepEntity>)

    @Transaction
    suspend fun upsertRecipeWithDetails(
        recipe: RecipeEntity,
        images: List<RecipeImageEntity>,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
    ) {
        upsertRecipe(recipe)
        upsertImages(images)
        upsertIngredients(ingredients)
        upsertSteps(steps)
    }

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE recipes SET is_favorited = :isFavorited WHERE id = :id")
    suspend fun setFavorited(id: String, isFavorited: Boolean)

    @Query("UPDATE recipes SET url = :url WHERE id = :imageId")
    suspend fun refreshImageUrl(imageId: String, url: String)
}
