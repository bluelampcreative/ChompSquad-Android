package com.bluelampcreative.chompsquad.ui.navigation

sealed interface NavEvent {
  data object GoBack : NavEvent

  data object NavigateToMain : NavEvent
}
