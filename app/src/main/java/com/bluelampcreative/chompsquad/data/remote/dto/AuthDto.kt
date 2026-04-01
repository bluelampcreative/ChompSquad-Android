package com.bluelampcreative.chompsquad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class GoogleAuthRequestDto(@SerialName("id_token") val idToken: String)

@Serializable data class EmailSignInRequestDto(val email: String, val password: String)

@Serializable
data class SignUpRequestDto(
    val email: String,
    val password: String,
    /** Derived from the local part of the email address (e.g. "johnny" from "johnny@email.com"). */
    @SerialName("screen_name") val screenName: String,
)

@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable data class RefreshRequestDto(@SerialName("refresh_token") val refreshToken: String)

@Serializable data class LogoutRequestDto(@SerialName("refresh_token") val refreshToken: String)
