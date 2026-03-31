package com.bluelampcreative.chompsquad.core

interface UIEventHandler<UIEventType> {
  fun handleEvent(event: UIEventType)
}
