package catalystpage.com.service.admin

import admin.dto.AuditLogDTO
import catalystpage.com.entity.admin.AuditLogEntity
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object AuditService {

    fun log(
        adminId: Int? = null,
        action: String,
        targetTable: String? = null,
        targetId: Int? = null
    ) = transaction {
        AuditLogEntity.new {
            this.adminId = adminId
            this.action = action
            this.targetTable = targetTable
            this.targetId = targetId
            this.createdAt = Instant.now()
        }
    }

    fun getAll(): List<AuditLogDTO> = transaction {
        AuditLogEntity.all().map { it.toDTO() }
    }
}