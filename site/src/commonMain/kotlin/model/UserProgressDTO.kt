package model

import kotlinx.serialization.Serializable


@Serializable
data class UserProgressDTO(
    val monthlyTotal: Int,
    val lifetimeTotal: Int
)