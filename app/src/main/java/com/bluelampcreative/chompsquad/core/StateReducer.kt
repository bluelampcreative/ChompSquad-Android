@file:OptIn(ExperimentalForInheritanceCoroutinesApi::class)

package com.bluelampcreative.chompsquad.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

interface IStateReducer<STATE, ACTION> : StateFlow<STATE> {
  fun dispatch(action: ACTION)
}

class StateReducer<STATE, ACTION>(
    initialState: STATE,
    reducedState: (STATE, ACTION) -> STATE,
    scope: CoroutineScope,
) : IStateReducer<STATE, ACTION> {

  override fun dispatch(action: ACTION) {
    actions.trySend(action)
  }

  private val actions = Channel<ACTION>(Channel.BUFFERED)

  private val stateFlow =
      actions
          .receiveAsFlow()
          .runningFold(initialState, reducedState)
          .stateIn(scope, Eagerly, initialState)

  override val value: STATE
    get() = stateFlow.value

  override val replayCache: List<STATE>
    get() = stateFlow.replayCache

  override suspend fun collect(collector: FlowCollector<STATE>): Nothing {
    stateFlow.collect(collector)
  }
}
