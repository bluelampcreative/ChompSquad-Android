package com.bluelampcreative.chompsquad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("subscription_tier") val subscriptionTier: String,
    @SerialName("scans_used_this_month") val scansUsedThisMonth: Int,
    @SerialName("scans_remaining") val scansRemaining: Int?,
    @SerialName("beta_expires_at") val betaExpiresAt: String?,
    @SerialName("created_at") val createdAt: String,
)

@Serializable data class FeedbackRequestDto(val message: String)
