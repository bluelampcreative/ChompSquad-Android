package com.bluelampcreative.chompsquad.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Base ViewModel for all ChompSquad screens.
 * - [viewState] — ongoing UI state driven by the [StateReducer] running fold. Dispatch pure state
 *   changes via [StateReducer.dispatch].
 * - [navEvents] — one-shot [UIEventType] values flowing VM → UI (navigation, toasts, etc.). Emit
 *   from subclasses via [navigate]; collect in the composable inside a [LaunchedEffect].
 * - [UIEventHandler.handleEvent] — UI → VM channel for button interactions and other UI-initiated
 *   side effects that don't fit the reducer.
 */
abstract class CoreViewModel<StateType, ActionType : ViewAction, UIEventType>(
    private val initialState: StateType
) : ViewModel(), IViewStateHolder<StateType>, UIEventHandler<UIEventType> {

  protected val state: IStateReducer<StateType, ActionType> by lazy {
    StateReducer(initialState, ::reducer, viewModelScope)
  }

  override val viewState: IStateReducer<StateType, ActionType>
    get() = state

  protected abstract fun reducer(state: StateType, action: ActionType): StateType

  // ── One-shot UI events (VM → UI) ──────────────────────────────────────────

  private val _navEvents = Channel<NavEvent>(Channel.BUFFERED)

  /** Collect in the composable with [LaunchedEffect] to handle one-shot events. */
  val navEvents = _navEvents.receiveAsFlow()

  /** Enqueues a one-shot event for the UI (navigation, snackbar, etc.). */
  protected fun navigate(event: NavEvent) {
    _navEvents.trySend(event)
  }
}
