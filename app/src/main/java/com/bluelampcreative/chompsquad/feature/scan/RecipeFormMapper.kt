package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.data.remote.dto.CreateRecipeRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.IngredientDto
import com.bluelampcreative.chompsquad.data.remote.dto.StepDto
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.Step

/**
 * Shared form-to-DTO mapping helpers used by both [ScanResultViewModel] and [ManualEntryViewModel].
 * Internal visibility keeps these out of the public API.
 */
internal fun ScanResultViewState.toRequestDto(originType: String): CreateRecipeRequestDto =
    CreateRecipeRequestDto(
        title = title,
        originType = originType,
        yieldAmount = yieldAmount.trimToNull(),
        yieldUnit = yieldUnit.trimToNull(),
        prepTime = prepTime.trim().toIntOrNull(),
        cookTime = cookTime.trim().toIntOrNull(),
        source = source.trimToNull(),
        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
        ingredients =
            ingredients.mapIndexed { index, ingredient -> ingredient.toIngredientDto(index + 1) },
        steps = steps.mapIndexed { index, step -> step.toStepDto(index + 1) },
    )

/** Returns a non-blank trimmed string, or null if blank. */
internal fun String.trimToNull(): String? = trim().ifBlank { null }

internal fun Ingredient.toIngredientDto(position: Int): IngredientDto =
    IngredientDto(
        id = id,
        position = position,
        quantity = quantity,
        unit = unit,
        name = name,
        prepNote = prepNote,
    )

internal fun Step.toStepDto(position: Int): StepDto =
    StepDto(id = id, position = position, instruction = instruction)

/**
 * Maps the field-change branches of a [ScanResultAction] onto [ScanResultViewState], returning null
 * for any non-field-change action. Extracted so both [ScanResultViewModel] and
 * [ManualEntryViewModel] can stay under the Detekt CyclomaticComplexMethod threshold.
 */
internal fun applyFieldChange(
    state: ScanResultViewState,
    action: ScanResultAction,
): ScanResultViewState? =
    when (action) {
      is ScanResultAction.TitleChanged -> state.copy(title = action.value)
      is ScanResultAction.YieldAmountChanged -> state.copy(yieldAmount = action.value)
      is ScanResultAction.YieldUnitChanged -> state.copy(yieldUnit = action.value)
      is ScanResultAction.PrepTimeChanged -> state.copy(prepTime = action.value)
      is ScanResultAction.CookTimeChanged -> state.copy(cookTime = action.value)
      is ScanResultAction.TotalTimeChanged -> state.copy(totalTime = action.value)
      is ScanResultAction.SourceChanged -> state.copy(source = action.value)
      is ScanResultAction.TagsChanged -> state.copy(tags = action.value)
      else -> null
    }
