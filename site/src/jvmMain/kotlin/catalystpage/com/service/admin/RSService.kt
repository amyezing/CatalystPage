package catalystpage.com.service.admin

import admin.dto.RecyclingScheduleDTO
import catalystpage.com.model.admin.table.RecyclingSchedules
import catalystpage.com.model.admin.table.toRecyclingScheduleDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object RecyclingScheduleService {
    fun getAll(): List<RecyclingScheduleDTO> = transaction {
        RecyclingSchedules.selectAll().map { it.toRecyclingScheduleDTO() }
    }

    fun getByZone(zoneId: Int): List<RecyclingScheduleDTO> = transaction {
        RecyclingSchedules.select { RecyclingSchedules.zoneId eq zoneId }
            .map { it.toRecyclingScheduleDTO() }
    }

    fun add(schedule: RecyclingScheduleDTO): RecyclingScheduleDTO = transaction {
        val id = RecyclingSchedules.insertAndGetId {
            it[zoneId] = schedule.zoneId
            it[scheduleDate] = LocalDate.parse(schedule.scheduleDate)
            it[type] = schedule.type
        }
        RecyclingSchedules.select { RecyclingSchedules.id eq id.value }
            .single().toRecyclingScheduleDTO()
    }

    fun update(id: Int, schedule: RecyclingScheduleDTO): Boolean = transaction {
        RecyclingSchedules.update({ RecyclingSchedules.id eq id }) {
            it[zoneId] = schedule.zoneId
            it[scheduleDate] = LocalDate.parse(schedule.scheduleDate)
            it[type] = schedule.type
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        RecyclingSchedules.deleteWhere { RecyclingSchedules.id eq id } > 0
    }
}
