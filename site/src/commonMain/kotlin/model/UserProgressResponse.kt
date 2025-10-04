package model

import kotlinx.serialization.Serializable

@Serializable
data class UserProgressResponse(
    val monthlyTotal: Int,
    val lifetimeTotal: Int
)