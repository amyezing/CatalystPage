package catalystpage.com.entity.admin

import admin.dto.AuditLogDTO
import catalystpage.com.model.admin.table.AuditLogTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AuditLogEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuditLogEntity>(AuditLogTable)

    var adminId by AuditLogTable.adminId
    var action by AuditLogTable.action
    var targetTable by AuditLogTable.targetTable
    var targetId by AuditLogTable.targetId
    var createdAt by AuditLogTable.createdAt

    fun toDTO() = AuditLogDTO(
        id = id.value,
        entity = targetTable ?: "", // map targetTable → entity for DTO
        action = action ?: "",
        description = null, // no description column in DB
        createdBy = adminId?.toString(),
        createdAt = createdAt.toString()
    )
}