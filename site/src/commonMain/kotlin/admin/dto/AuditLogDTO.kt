package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuditLogDTO(
    val id: Int,
    val entity: String,
    val action: String,
    val description: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null
)