package com.bluelampcreative.chompsquad.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

abstract class CoreViewModel<StateType, ActionType : ViewAction, UIEventType>(
    private val initialState: StateType
) : ViewModel(), IViewStateHolder<StateType>, UIEventHandler<UIEventType> {

  protected val state: IStateReducer<StateType, ActionType> by lazy {
    StateReducer(initialState, ::reducer, viewModelScope)
  }

  protected abstract fun reducer(state: StateType, action: ActionType): StateType

  override val viewState: IStateReducer<StateType, ActionType>
    get() = state
}
