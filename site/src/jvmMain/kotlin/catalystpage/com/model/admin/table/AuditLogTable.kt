package catalystpage.com.model.admin.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp


object AuditLogTable : IntIdTable("audit_logs") {
    val adminId = integer("admin_id").nullable()
    val action = text("action").nullable()
    val targetTable = varchar("target_table", 100).nullable()
    val targetId = integer("target_id").nullable()
    val createdAt = timestamp("created_at")
}