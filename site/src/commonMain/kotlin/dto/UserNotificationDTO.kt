package dto

import kotlinx.serialization.Serializable

@Serializable
data class UserNotificationDTO(
    val firebaseUid: String,
    val notifyMarketing: Boolean,
    val notifyOrderUpdates: Boolean,
    val updatedAt: String? = null
)