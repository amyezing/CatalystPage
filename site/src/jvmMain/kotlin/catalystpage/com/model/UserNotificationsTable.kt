package catalystpage.com.model

import dto.UserNotificationDTO
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserNotificationsTable : Table("user_notifications") {
    val firebaseUid = varchar("firebase_uid", 128)
    val notifyMarketing = bool("notify_marketing").default(false)
    val notifyOrderUpdates = bool("notify_order_updates").default(true)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
}

fun ResultRow.toNotificationDTO() = UserNotificationDTO(
    firebaseUid = this[UserNotificationsTable.firebaseUid],
    notifyMarketing = this[UserNotificationsTable.notifyMarketing],
    notifyOrderUpdates = this[UserNotificationsTable.notifyOrderUpdates],
    updatedAt = this[UserNotificationsTable.updatedAt].toString()
)

