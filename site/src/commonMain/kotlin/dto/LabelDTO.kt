package dto

import kotlinx.serialization.Serializable

@Serializable
data class LabelDTO(
    val id: Int,
    val name: String,
    val color: String? = null,
    val priority: Int = 0
)