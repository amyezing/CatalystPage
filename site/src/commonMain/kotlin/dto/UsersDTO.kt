package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserDTO(
    @SerialName("id") val id: Int? = null,
    @SerialName("firebase_uid") val firebaseUid: String,
    @SerialName("email") val email: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("role") val roleRaw: String? = null,
    @Transient val role: Role? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("eco_points") val ecoPoints: Int = 0,
    val notifications: UserNotificationDTO? = null,
    val zoneId: Int? = null

)

@Serializable
enum class Role {
    @SerialName("USER") USER,
    @SerialName("ADMIN") ADMIN;
    companion object {
        fun fromStringOrNull(value: String?): Role? {
            return try {
                value?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.uppercase()
                    ?.let { valueOf(it) }
            } catch (e: Exception) {
                null
            }
        }
    }
}