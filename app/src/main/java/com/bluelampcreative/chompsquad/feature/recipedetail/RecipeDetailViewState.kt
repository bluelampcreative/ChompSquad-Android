package com.bluelampcreative.chompsquad.feature.recipedetail

import com.bluelampcreative.chompsquad.core.ViewAction
import com.bluelampcreative.chompsquad.domain.model.Recipe

data class RecipeDetailViewState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface RecipeDetailAction : ViewAction {
  data class RecipeLoaded(val recipe: Recipe) : RecipeDetailAction

  data class FetchFailed(val message: String) : RecipeDetailAction
}

sealed interface RecipeDetailUiEvent {
  data object OnBack : RecipeDetailUiEvent

  data class OnImageRefreshNeeded(val imageId: String, val blobPath: String) : RecipeDetailUiEvent
}
