package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.core.ViewAction
import com.bluelampcreative.chompsquad.domain.model.Ingredient

/**
 * Mutable representation of a single ingredient row in the editor. Fields are plain strings so text
 * fields have full edit freedom; they are converted back to [Ingredient] on save.
 *
 * A stable [id] is required by the reorder library for item key tracking. Existing ingredients keep
 * their server-assigned ID; newly added rows get a random UUID.
 */
data class EditableIngredient(
    val id: String,
    val quantity: String,
    val unit: String,
    val name: String,
    val prepNote: String,
)

data class IngredientEditorViewState(
    val ingredients: List<EditableIngredient> = emptyList(),
)

sealed interface IngredientEditorAction : ViewAction {
  data class IngredientsLoaded(val ingredients: List<EditableIngredient>) : IngredientEditorAction

  data object IngredientAdded : IngredientEditorAction

  data class IngredientRemoved(val id: String) : IngredientEditorAction

  data class IngredientMoved(val fromIndex: Int, val toIndex: Int) : IngredientEditorAction

  data class QuantityChanged(val id: String, val value: String) : IngredientEditorAction

  data class UnitChanged(val id: String, val value: String) : IngredientEditorAction

  data class NameChanged(val id: String, val value: String) : IngredientEditorAction

  data class PrepNoteChanged(val id: String, val value: String) : IngredientEditorAction
}

sealed interface IngredientEditorUiEvent {
  data object OnAddIngredient : IngredientEditorUiEvent

  data class OnRemoveIngredient(val id: String) : IngredientEditorUiEvent

  data class OnMove(val fromIndex: Int, val toIndex: Int) : IngredientEditorUiEvent

  data class OnQuantityChanged(val id: String, val value: String) : IngredientEditorUiEvent

  data class OnUnitChanged(val id: String, val value: String) : IngredientEditorUiEvent

  data class OnNameChanged(val id: String, val value: String) : IngredientEditorUiEvent

  data class OnPrepNoteChanged(val id: String, val value: String) : IngredientEditorUiEvent

  /** Write the edited list back to [ScanSessionRepository] and pop the screen. */
  data object OnDone : IngredientEditorUiEvent

  /** Discard edits and pop the screen without saving. */
  data object OnClose : IngredientEditorUiEvent
}
