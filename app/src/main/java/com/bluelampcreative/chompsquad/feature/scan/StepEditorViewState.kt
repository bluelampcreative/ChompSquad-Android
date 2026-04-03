package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.core.ViewAction

/**
 * Mutable representation of a single step row in the editor. Only [instruction] is editable;
 * [position] is derived from the list index on save and not stored here.
 *
 * A stable [id] is required by the reorder library for item key tracking. Existing steps keep their
 * server-assigned ID; newly added rows get a random UUID.
 */
data class EditableStep(
    val id: String,
    val instruction: String,
)

data class StepEditorViewState(
    val steps: List<EditableStep> = emptyList(),
)

sealed interface StepEditorAction : ViewAction {
  data class StepsLoaded(val steps: List<EditableStep>) : StepEditorAction

  data object StepAdded : StepEditorAction

  data class StepRemoved(val id: String) : StepEditorAction

  data class StepMoved(val fromIndex: Int, val toIndex: Int) : StepEditorAction

  data class InstructionChanged(val id: String, val value: String) : StepEditorAction
}

sealed interface StepEditorUiEvent {
  data object OnAddStep : StepEditorUiEvent

  data class OnRemoveStep(val id: String) : StepEditorUiEvent

  data class OnMove(val fromIndex: Int, val toIndex: Int) : StepEditorUiEvent

  data class OnInstructionChanged(val id: String, val value: String) : StepEditorUiEvent

  /** Write the edited list back to [ScanSessionRepository] and pop the screen. */
  data object OnDone : StepEditorUiEvent

  /** Discard edits and pop the screen without saving. */
  data object OnClose : StepEditorUiEvent
}
