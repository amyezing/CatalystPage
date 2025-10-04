package catalystpage.com.model.admin.table

import admin.dto.RecyclingScheduleDTO
import admin.dto.ScheduleType
import catalystpage.com.model.community.Zones
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object RecyclingSchedules : IntIdTable("recycling_schedule") {
    val zoneId = integer("zone_id").references(Zones.id)
    val scheduleDate = date("schedule_date")
    val type = enumerationByName("type", 20, ScheduleType::class)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toRecyclingScheduleDTO(): RecyclingScheduleDTO {
    return RecyclingScheduleDTO(
        id = this[RecyclingSchedules.id].value,
        zoneId = this[RecyclingSchedules.zoneId],
        scheduleDate = this[RecyclingSchedules.scheduleDate].toString(),
        type = this[RecyclingSchedules.type]
    )
}