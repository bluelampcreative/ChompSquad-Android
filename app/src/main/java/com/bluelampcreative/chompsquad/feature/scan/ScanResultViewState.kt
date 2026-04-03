package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.core.ViewAction
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.domain.model.Step

data class ScanResultViewState(
    val isLoading: Boolean = true,
    val title: String = "",
    val yieldAmount: String = "",
    val yieldUnit: String = "",
    val prepTime: String = "",
    val cookTime: String = "",
    val totalTime: String = "",
    val source: String = "",
    /** Comma-separated tag string for inline editing; split on save. */
    val tags: String = "",
    val heroImageUrl: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
)

sealed interface ScanResultAction : ViewAction {
  data class RecipeLoaded(val recipe: Recipe) : ScanResultAction

  data class TitleChanged(val value: String) : ScanResultAction

  data class YieldAmountChanged(val value: String) : ScanResultAction

  data class YieldUnitChanged(val value: String) : ScanResultAction

  data class PrepTimeChanged(val value: String) : ScanResultAction

  data class CookTimeChanged(val value: String) : ScanResultAction

  data class TotalTimeChanged(val value: String) : ScanResultAction

  data class SourceChanged(val value: String) : ScanResultAction

  data class TagsChanged(val value: String) : ScanResultAction
}

sealed interface ScanResultUiEvent {
  data class OnTitleChanged(val value: String) : ScanResultUiEvent

  data class OnYieldAmountChanged(val value: String) : ScanResultUiEvent

  data class OnYieldUnitChanged(val value: String) : ScanResultUiEvent

  data class OnPrepTimeChanged(val value: String) : ScanResultUiEvent

  data class OnCookTimeChanged(val value: String) : ScanResultUiEvent

  data class OnTotalTimeChanged(val value: String) : ScanResultUiEvent

  data class OnSourceChanged(val value: String) : ScanResultUiEvent

  data class OnTagsChanged(val value: String) : ScanResultUiEvent

  /** Placeholder — navigate to ingredient list editor (task 2.5). */
  data object OnEditIngredients : ScanResultUiEvent

  /** Placeholder — navigate to steps editor (task 2.6). */
  data object OnEditSteps : ScanResultUiEvent

  data object OnSave : ScanResultUiEvent

  data object OnClose : ScanResultUiEvent
}
