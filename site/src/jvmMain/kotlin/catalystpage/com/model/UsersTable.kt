package catalystpage.com.model

import dto.Role
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : IntIdTable("users") {
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255).nullable()
    val name = varchar("name", 100).nullable()
    val phone = varchar("phone", 20).nullable()
    val role = enumerationByName("role", 10, Role::class).default(Role.USER)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}