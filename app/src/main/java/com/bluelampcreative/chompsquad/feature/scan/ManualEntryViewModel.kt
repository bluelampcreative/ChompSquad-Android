package com.bluelampcreative.chompsquad.feature.scan

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.remote.RecipeRepository
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * ViewModel for the manual recipe entry screen (task 2.9).
 *
 * Reuses [ScanResultViewState], [ScanResultAction], and [ScanResultUiEvent] — the form fields and
 * editing behaviour are identical to the scan result review screen. The only differences are:
 * - Initial state is blank (no scan result to pre-populate).
 * - [originType] is `"manual"` in the POST body.
 */
@KoinViewModel
class ManualEntryViewModel(
    private val scanSessionRepository: ScanSessionRepository,
    private val recipeRepository: RecipeRepository,
) :
    CoreViewModel<ScanResultViewState, ScanResultAction, ScanResultUiEvent>(
        ScanResultViewState(isLoading = false)
    ) {

  // Guards against duplicate POSTs from rapid taps before isSaving propagates through the
  // StateReducer channel. Checked and set synchronously before the coroutine is launched.
  private val isSaveInFlight = AtomicBoolean(false)

  init {
    // Collect ingredient edits written back by IngredientEditorViewModel on Done.
    viewModelScope.launch {
      scanSessionRepository.ingredientEdits.filterNotNull().collect { ingredients ->
        state.dispatch(ScanResultAction.IngredientsUpdated(ingredients))
      }
    }

    // Collect step edits written back by StepEditorViewModel on Done.
    viewModelScope.launch {
      scanSessionRepository.stepEdits.filterNotNull().collect { steps ->
        state.dispatch(ScanResultAction.StepsUpdated(steps))
      }
    }
  }

  override fun reducer(
      state: ScanResultViewState,
      action: ScanResultAction,
  ): ScanResultViewState {
    applyFieldChange(state, action)?.let {
      return it
    }
    return when (action) {
      is ScanResultAction.IngredientsUpdated -> state.copy(ingredients = action.ingredients)
      is ScanResultAction.StepsUpdated -> state.copy(steps = action.steps)
      ScanResultAction.SaveStarted -> state.copy(isSaving = true, saveError = null)
      ScanResultAction.SaveSucceeded -> state.copy(isSaving = false, saveSuccess = true)
      is ScanResultAction.SaveFailed -> state.copy(isSaving = false, saveError = action.message)
      else -> state
    }
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
      ScanResultUiEvent.OnEditSteps -> {
        scanSessionRepository.setStepEdits(state.value.steps)
        navigate(NavEvent.NavigateToStepEditor)
      }
      ScanResultUiEvent.OnSave -> handleSave()
      ScanResultUiEvent.OnNavigateAfterSave -> {
        scanSessionRepository.clear()
        navigate(NavEvent.NavigateToMain)
      }
      ScanResultUiEvent.OnClose -> navigate(NavEvent.GoBack)
    }
  }

  private fun handleSave() {
    if (!isSaveInFlight.compareAndSet(false, true)) return
    val current = state.value
    state.dispatch(ScanResultAction.SaveStarted)
    viewModelScope.launch {
      recipeRepository
          .saveRecipe(current.toRequestDto("manual"))
          .onSuccess { state.dispatch(ScanResultAction.SaveSucceeded) }
          .onFailure { error ->
            isSaveInFlight.set(false)
            state.dispatch(ScanResultAction.SaveFailed(error.message ?: "Save failed"))
          }
    }
  }
}
