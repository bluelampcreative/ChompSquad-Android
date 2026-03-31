package com.bluelampcreative.chompsquad.core

import kotlinx.coroutines.flow.StateFlow

interface IViewStateHolder<ViewStateType> {
  val viewState: StateFlow<ViewStateType>
}
