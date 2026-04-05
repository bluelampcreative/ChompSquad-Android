package com.bluelampcreative.chompsquad.feature.recipedetail

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.remote.RecipeRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RecipeDetailViewModel(
    @InjectedParam private val recipeId: String,
    private val recipeRepository: RecipeRepository,
) :
    CoreViewModel<RecipeDetailViewState, RecipeDetailAction, RecipeDetailUiEvent>(
        RecipeDetailViewState()
    ) {

  init {
    // Observe cached recipe — updates automatically when Room changes (e.g. after image refresh).
    viewModelScope.launch {
      recipeRepository.observeRecipe(recipeId).collect { recipe ->
        if (recipe != null) state.dispatch(RecipeDetailAction.RecipeLoaded(recipe))
      }
    }
    // Fetch full detail from server to populate / refresh Room.
    viewModelScope.launch {
      recipeRepository.fetchRecipe(recipeId).onFailure { error ->
        state.dispatch(RecipeDetailAction.FetchFailed(error.message ?: "Failed to load recipe"))
      }
    }
  }

  override fun reducer(
      state: RecipeDetailViewState,
      action: RecipeDetailAction,
  ): RecipeDetailViewState =
      when (action) {
        is RecipeDetailAction.RecipeLoaded ->
            state.copy(recipe = action.recipe, isLoading = false, error = null)
        is RecipeDetailAction.FetchFailed -> state.copy(isLoading = false, error = action.message)
      }

  override fun handleEvent(event: RecipeDetailUiEvent) {
    when (event) {
      RecipeDetailUiEvent.OnBack -> navigate(NavEvent.GoBack)
      is RecipeDetailUiEvent.OnImageRefreshNeeded ->
          viewModelScope.launch {
            if (event.blobPath.isNotBlank()) {
              recipeRepository.refreshHeroImageUrl(event.imageId, event.blobPath)
            }
          }
    }
  }
}
