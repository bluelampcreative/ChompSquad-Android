package com.bluelampcreative.chompsquad.feature.scan

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScanResultViewModel(
    private val scanSessionRepository: ScanSessionRepository,
) : CoreViewModel<ScanResultViewState, ScanResultAction, ScanResultUiEvent>(ScanResultViewState()) {

  init {
    val recipe = scanSessionRepository.getScanResult()
    if (recipe != null) {
      state.dispatch(ScanResultAction.RecipeLoaded(recipe))
    } else {
      // No result in session — navigate back (handles process death or unexpected entry).
      navigate(NavEvent.GoBack)
    }

    // Collect ingredient edits written back by IngredientEditorViewModel on Done.
    viewModelScope.launch {
      scanSessionRepository.ingredientEdits.filterNotNull().collect { ingredients ->
        state.dispatch(ScanResultAction.IngredientsUpdated(ingredients))
      }
    }
  }

  override fun reducer(
      state: ScanResultViewState,
      action: ScanResultAction,
  ): ScanResultViewState =
      when (action) {
        is ScanResultAction.RecipeLoaded -> action.recipe.toViewState()
        is ScanResultAction.IngredientsUpdated -> state.copy(ingredients = action.ingredients)
        is ScanResultAction.TitleChanged -> state.copy(title = action.value)
        is ScanResultAction.YieldAmountChanged -> state.copy(yieldAmount = action.value)
        is ScanResultAction.YieldUnitChanged -> state.copy(yieldUnit = action.value)
        is ScanResultAction.PrepTimeChanged -> state.copy(prepTime = action.value)
        is ScanResultAction.CookTimeChanged -> state.copy(cookTime = action.value)
        is ScanResultAction.TotalTimeChanged -> state.copy(totalTime = action.value)
        is ScanResultAction.SourceChanged -> state.copy(source = action.value)
        is ScanResultAction.TagsChanged -> state.copy(tags = action.value)
      }

  override fun handleEvent(event: ScanResultUiEvent) {
    when (event) {
      is ScanResultUiEvent.OnTitleChanged ->
          state.dispatch(ScanResultAction.TitleChanged(event.value))
      is ScanResultUiEvent.OnYieldAmountChanged ->
          state.dispatch(ScanResultAction.YieldAmountChanged(event.value))
      is ScanResultUiEvent.OnYieldUnitChanged ->
          state.dispatch(ScanResultAction.YieldUnitChanged(event.value))
      is ScanResultUiEvent.OnPrepTimeChanged ->
          state.dispatch(ScanResultAction.PrepTimeChanged(event.value))
      is ScanResultUiEvent.OnCookTimeChanged ->
          state.dispatch(ScanResultAction.CookTimeChanged(event.value))
      is ScanResultUiEvent.OnTotalTimeChanged ->
          state.dispatch(ScanResultAction.TotalTimeChanged(event.value))
      is ScanResultUiEvent.OnSourceChanged ->
          state.dispatch(ScanResultAction.SourceChanged(event.value))
      is ScanResultUiEvent.OnTagsChanged ->
          state.dispatch(ScanResultAction.TagsChanged(event.value))
      ScanResultUiEvent.OnEditIngredients -> {
        scanSessionRepository.setIngredientEdits(state.value.ingredients)
        navigate(NavEvent.NavigateToIngredientEditor)
      }
      // TODO(task 2.6): navigate to steps editor
      ScanResultUiEvent.OnEditSteps -> Unit
      // TODO(task 2.7): POST recipe to /v1/recipes before navigating; clear session on success
      ScanResultUiEvent.OnSave -> {
        scanSessionRepository.clear()
        navigate(NavEvent.NavigateToMain)
      }
      ScanResultUiEvent.OnClose -> {
        scanSessionRepository.clear()
        navigate(NavEvent.GoBack)
      }
    }
  }
}

private fun Recipe.toViewState(): ScanResultViewState =
    ScanResultViewState(
        isLoading = false,
        title = title,
        yieldAmount = yieldAmount ?: "",
        yieldUnit = yieldUnit ?: "",
        prepTime = prepTime?.toString() ?: "",
        cookTime = cookTime?.toString() ?: "",
        totalTime = totalTime?.toString() ?: "",
        source = source ?: "",
        tags = tags.joinToString(", "),
        heroImageUrl = images.minByOrNull { it.position }?.url,
        ingredients = ingredients,
        steps = steps,
    )
