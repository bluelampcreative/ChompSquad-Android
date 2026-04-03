package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import java.util.UUID
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class IngredientEditorViewModel(
    private val scanSessionRepository: ScanSessionRepository,
) :
    CoreViewModel<IngredientEditorViewState, IngredientEditorAction, IngredientEditorUiEvent>(
        IngredientEditorViewState()
    ) {

  init {
    val initial = scanSessionRepository.ingredientEdits.value ?: emptyList()
    state.dispatch(IngredientEditorAction.IngredientsLoaded(initial.toEditables()))
  }

  override fun reducer(
      state: IngredientEditorViewState,
      action: IngredientEditorAction,
  ): IngredientEditorViewState =
      when (action) {
        is IngredientEditorAction.IngredientsLoaded -> state.copy(ingredients = action.ingredients)
        is IngredientEditorAction.IngredientAdded -> {
          val new =
              EditableIngredient(
                  id = UUID.randomUUID().toString(),
                  quantity = "",
                  unit = "",
                  name = "",
                  prepNote = "",
              )
          state.copy(ingredients = state.ingredients + new)
        }
        is IngredientEditorAction.IngredientRemoved ->
            state.copy(ingredients = state.ingredients.filter { it.id != action.id })
        is IngredientEditorAction.IngredientMoved -> {
          val mutable = state.ingredients.toMutableList()
          mutable.add(action.toIndex, mutable.removeAt(action.fromIndex))
          state.copy(ingredients = mutable.toList())
        }
        is IngredientEditorAction.QuantityChanged ->
            state.copy(
                ingredients =
                    state.ingredients.map {
                      if (it.id == action.id) it.copy(quantity = action.value) else it
                    }
            )
        is IngredientEditorAction.UnitChanged ->
            state.copy(
                ingredients =
                    state.ingredients.map {
                      if (it.id == action.id) it.copy(unit = action.value) else it
                    }
            )
        is IngredientEditorAction.NameChanged ->
            state.copy(
                ingredients =
                    state.ingredients.map {
                      if (it.id == action.id) it.copy(name = action.value) else it
                    }
            )
        is IngredientEditorAction.PrepNoteChanged ->
            state.copy(
                ingredients =
                    state.ingredients.map {
                      if (it.id == action.id) it.copy(prepNote = action.value) else it
                    }
            )
      }

  override fun handleEvent(event: IngredientEditorUiEvent) {
    when (event) {
      IngredientEditorUiEvent.OnAddIngredient ->
          state.dispatch(IngredientEditorAction.IngredientAdded)
      is IngredientEditorUiEvent.OnRemoveIngredient ->
          state.dispatch(IngredientEditorAction.IngredientRemoved(event.id))
      is IngredientEditorUiEvent.OnMove ->
          state.dispatch(IngredientEditorAction.IngredientMoved(event.fromIndex, event.toIndex))
      is IngredientEditorUiEvent.OnQuantityChanged ->
          state.dispatch(IngredientEditorAction.QuantityChanged(event.id, event.value))
      is IngredientEditorUiEvent.OnUnitChanged ->
          state.dispatch(IngredientEditorAction.UnitChanged(event.id, event.value))
      is IngredientEditorUiEvent.OnNameChanged ->
          state.dispatch(IngredientEditorAction.NameChanged(event.id, event.value))
      is IngredientEditorUiEvent.OnPrepNoteChanged ->
          state.dispatch(IngredientEditorAction.PrepNoteChanged(event.id, event.value))
      IngredientEditorUiEvent.OnDone -> {
        val updated =
            state.value.ingredients.mapIndexed { index, ingredient ->
              ingredient.toDomain(position = index + 1)
            }
        scanSessionRepository.setIngredientEdits(updated)
        navigate(NavEvent.GoBack)
      }
      IngredientEditorUiEvent.OnClose -> navigate(NavEvent.GoBack)
    }
  }
}

private fun List<Ingredient>.toEditables(): List<EditableIngredient> = map { ingredient ->
  EditableIngredient(
      id = ingredient.id,
      quantity = ingredient.quantity ?: "",
      unit = ingredient.unit ?: "",
      name = ingredient.name,
      prepNote = ingredient.prepNote ?: "",
  )
}

private fun EditableIngredient.toDomain(position: Int): Ingredient =
    Ingredient(
        id = id,
        position = position,
        quantity = quantity.ifBlank { null },
        unit = unit.ifBlank { null },
        name = name,
        prepNote = prepNote.ifBlank { null },
    )
