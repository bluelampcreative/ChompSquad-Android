package com.bluelampcreative.chompsquad.models

sealed interface Outcome<out T>

data class Success<T>(val data: T): Outcome<T>

sealed interface Failure: Outcome<Nothing> {
    data class Error(val error: Throwable): Failure
    object Empty: Failure
}

fun <T> Outcome<T>.onSuccess(callback: (data : T) -> Unit): Outcome<T> {
    if (this is Success) {
        callback(data)
    }
    return this
}

fun <T> Outcome<T>.onFailure(callback: (failure: Failure) -> Unit): Outcome<T> {
    if (this is Failure) {
        callback(this)
    }
    return this
}