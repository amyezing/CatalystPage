package dto

import kotlinx.serialization.Serializable


@Serializable
data class EcoPointTransactionDTO(
    val id: Int? = null,
    val userId: Int,
    val points: Int,
    val reason: String = "recycling_scan",
    val createdAt: String? = null
)