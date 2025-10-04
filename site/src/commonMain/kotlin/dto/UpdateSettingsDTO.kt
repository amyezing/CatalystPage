package dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSettingsDTO(
    val notifyOrderUpdates: Boolean? = null,
    val notifyMarketing: Boolean? = null
)
