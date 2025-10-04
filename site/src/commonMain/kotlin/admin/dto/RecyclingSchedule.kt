package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecyclingScheduleDTO(
    val id: Int? = null,
    val zoneId: Int,
    val zoneName: String? = null,
    val scheduleDate: String, // ISO date
    val type: ScheduleType = ScheduleType.RECYCLING
)

@Serializable
enum class ScheduleType {
    RECYCLING,
    SHIPPING
}