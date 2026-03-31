package com.bluelampcreative.chompsquad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bluelampcreative.chompsquad.data.local.dao.RecipeDao
import com.bluelampcreative.chompsquad.data.local.entity.IngredientEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeImageEntity
import com.bluelampcreative.chompsquad.data.local.entity.StepEntity

@Database(
    entities =
        [
            RecipeEntity::class,
            RecipeImageEntity::class,
            IngredientEntity::class,
            StepEntity::class,
        ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun recipeDao(): RecipeDao
}
