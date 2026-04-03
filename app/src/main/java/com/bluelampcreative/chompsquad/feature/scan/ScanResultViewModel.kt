package com.bluelampcreative.chompsquad.feature.scan

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.remote.RecipeRepository
import com.bluelampcreative.chompsquad.data.remote.dto.CreateRecipeRequestDto
import com.bluelampcreative.chompsquad.data.remote.dto.IngredientDto
import com.bluelampcreative.chompsquad.data.remote.dto.StepDto
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.domain.model.Step
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScanResultViewModel(
    private val scanSessionRepository: ScanSessionRepository,
    private val recipeRepository: RecipeRepository,
) : CoreViewModel<ScanResultViewState, ScanResultAction, ScanResultUiEvent>(ScanResultViewState()) {

  // Guards against duplicate POSTs from rapid taps before isSaving propagates through the
  // StateReducer channel. Checked and set synchronously before the coroutine is launched.
  private val isSaveInFlight = AtomicBoolean(false)

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

    // Collect step edits written back by StepEditorViewModel on Done.
    viewModelScope.launch {
      scanSessionRepository.stepEdits.filterNotNull().collect { steps ->
        state.dispatch(ScanResultAction.StepsUpdated(steps))
      }
    }
  }

  // Field-change actions map 1-to-1 from view state field → action value. Extracted here so
  // reducer() stays under the Detekt CyclomaticComplexMethod threshold.
  private fun applyFieldChange(
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

  override fun reducer(
      state: ScanResultViewState,
      action: ScanResultAction,
  ): ScanResultViewState {
    applyFieldChange(state, action)?.let {
      return it
    }
    return when (action) {
      is ScanResultAction.RecipeLoaded -> action.recipe.toViewState()
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
      ScanResultUiEvent.OnClose -> {
        scanSessionRepository.clear()
        navigate(NavEvent.GoBack)
      }
    }
  }

  private fun handleSave() {
    if (!isSaveInFlight.compareAndSet(false, true)) return
    val current = state.value
    state.dispatch(ScanResultAction.SaveStarted)
    viewModelScope.launch {
      recipeRepository
          .saveRecipe(current.toRequestDto())
          .onSuccess { state.dispatch(ScanResultAction.SaveSucceeded) }
          .onFailure { error ->
            isSaveInFlight.set(false)
            state.dispatch(ScanResultAction.SaveFailed(error.message ?: "Save failed"))
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

private fun ScanResultViewState.toRequestDto(): CreateRecipeRequestDto =
    CreateRecipeRequestDto(
        title = title,
        originType = "scanned",
        yieldAmount = yieldAmount.trimToNull(),
        yieldUnit = yieldUnit.trimToNull(),
        prepTime = prepTime.trim().toIntOrNull(),
        cookTime = cookTime.trim().toIntOrNull(),
        source = source.trimToNull(),
        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
        ingredients = ingredients.mapIndexed { index, ingredient -> ingredient.toDto(index + 1) },
        steps = steps.mapIndexed { index, step -> step.toDto(index + 1) },
    )

private fun String.trimToNull(): String? = trim().ifBlank { null }

private fun Ingredient.toDto(position: Int): IngredientDto =
    IngredientDto(
        id = id,
        position = position,
        quantity = quantity,
        unit = unit,
        name = name,
        prepNote = prepNote,
    )

private fun Step.toDto(position: Int): StepDto =
    StepDto(id = id, position = position, instruction = instruction)
