package com.bluelampcreative.chompsquad.data.mapper

import com.bluelampcreative.chompsquad.data.local.entity.IngredientEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeImageEntity
import com.bluelampcreative.chompsquad.data.local.entity.RecipeWithDetails
import com.bluelampcreative.chompsquad.data.local.entity.StepEntity
import com.bluelampcreative.chompsquad.data.remote.dto.RecipeDto
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.OriginType
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.domain.model.RecipeImage
import com.bluelampcreative.chompsquad.domain.model.Step

// ── DTO → Entity ──────────────────────────────────────────────────────────────
// Mapping implementation is held until the data management pattern is
// demonstrated (see CHECKLIST task 0.6 note). Signatures are defined here
// so dependent code can compile and the pattern is clear.

fun RecipeDto.toEntity(): RecipeEntity = TODO("Implement after data pattern demo")

fun RecipeDto.toImageEntities(): List<RecipeImageEntity> = TODO("Implement after data pattern demo")

fun RecipeDto.toIngredientEntities(): List<IngredientEntity> = TODO("Implement after data pattern demo")

fun RecipeDto.toStepEntities(): List<StepEntity> = TODO("Implement after data pattern demo")

// ── Entity → Domain ───────────────────────────────────────────────────────────

fun RecipeWithDetails.toDomain(): Recipe = TODO("Implement after data pattern demo")

// ── Domain helpers ────────────────────────────────────────────────────────────

fun String.toOriginType(): OriginType = when (this) {
    "scanned"     -> OriginType.SCANNED
    "manual"      -> OriginType.MANUAL
    "social_save" -> OriginType.SOCIAL_SAVE
    else          -> OriginType.MANUAL
}
