package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserDTO(
    @SerialName("firebase_uid") val firebaseUid: String,
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null
)