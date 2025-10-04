package dto

import kotlinx.serialization.Serializable


@Serializable
data class UserProfile(
    val username: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null
)