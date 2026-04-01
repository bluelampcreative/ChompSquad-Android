package com.bluelampcreative.chompsquad.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Singleton

/**
 * Broadcasts authentication lifecycle events that originate outside the ViewModel layer (e.g. the
 * Ktor bearer-token refresh pipeline). Collected by the root navigation composable to redirect the
 * user to Sign In when a session can no longer be refreshed.
 */
@Singleton
class AuthEventBus {
  private val _sessionExpired =
      MutableSharedFlow<Unit>(
          extraBufferCapacity = 1,
          onBufferOverflow = BufferOverflow.DROP_OLDEST,
      )

  val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

  /** Called by the Ktor auth plugin when a token refresh attempt fails. Non-suspending. */
  fun emitSessionExpired() {
    _sessionExpired.tryEmit(Unit)
  }
}
