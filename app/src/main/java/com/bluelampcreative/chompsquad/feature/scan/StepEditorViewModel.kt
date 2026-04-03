package com.bluelampcreative.chompsquad.feature.scan

import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.scanner.ScanSessionRepository
import com.bluelampcreative.chompsquad.domain.model.Step
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import java.util.UUID
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class StepEditorViewModel(
    private val scanSessionRepository: ScanSessionRepository,
) : CoreViewModel<StepEditorViewState, StepEditorAction, StepEditorUiEvent>(StepEditorViewState()) {

  init {
    val initial = scanSessionRepository.stepEdits.value ?: emptyList()
    state.dispatch(StepEditorAction.StepsLoaded(initial.toEditables()))
  }

  override fun reducer(
      state: StepEditorViewState,
      action: StepEditorAction,
  ): StepEditorViewState =
      when (action) {
        is StepEditorAction.StepsLoaded -> state.copy(steps = action.steps)
        is StepEditorAction.StepAdded -> {
          val new = EditableStep(id = UUID.randomUUID().toString(), instruction = "")
          state.copy(steps = state.steps + new)
        }
        is StepEditorAction.StepRemoved ->
            state.copy(steps = state.steps.filter { it.id != action.id })
        is StepEditorAction.StepMoved -> {
          val mutable = state.steps.toMutableList()
          mutable.add(action.toIndex, mutable.removeAt(action.fromIndex))
          state.copy(steps = mutable.toList())
        }
        is StepEditorAction.InstructionChanged ->
            state.copy(
                steps =
                    state.steps.map {
                      if (it.id == action.id) it.copy(instruction = action.value) else it
                    }
            )
      }

  override fun handleEvent(event: StepEditorUiEvent) {
    when (event) {
      StepEditorUiEvent.OnAddStep -> state.dispatch(StepEditorAction.StepAdded)
      is StepEditorUiEvent.OnRemoveStep -> state.dispatch(StepEditorAction.StepRemoved(event.id))
      is StepEditorUiEvent.OnMove ->
          state.dispatch(StepEditorAction.StepMoved(event.fromIndex, event.toIndex))
      is StepEditorUiEvent.OnInstructionChanged ->
          state.dispatch(StepEditorAction.InstructionChanged(event.id, event.value))
      StepEditorUiEvent.OnDone -> {
        val updated =
            state.value.steps.mapIndexed { index, step -> step.toDomain(position = index + 1) }
        scanSessionRepository.setStepEdits(updated)
        navigate(NavEvent.GoBack)
      }
      StepEditorUiEvent.OnClose -> {
        scanSessionRepository.clearStepEdits()
        navigate(NavEvent.GoBack)
      }
    }
  }
}

private fun List<Step>.toEditables(): List<EditableStep> = map { step ->
  EditableStep(id = step.id, instruction = step.instruction)
}

private fun EditableStep.toDomain(position: Int): Step =
    Step(id = id, position = position, instruction = instruction)
