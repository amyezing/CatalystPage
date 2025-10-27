package catalystpage.com.service

import catalystpage.com.model.UserNotificationsTable
import catalystpage.com.model.toNotificationDTO
import dto.UpdateSettingsDTO
import dto.UserNotificationDTO
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object NotificationService {

    fun getByFirebaseUid(firebaseUid: String): UserNotificationDTO? = transaction {
        UserNotificationsTable
            .select { UserNotificationsTable.firebaseUid eq firebaseUid }
            .map { it.toNotificationDTO() }
            .singleOrNull()
    }

    fun updateSettingsByFirebaseUid(firebaseUid: String, dto: UpdateSettingsDTO): Boolean = transaction {
        val updatedRows = UserNotificationsTable.update({ UserNotificationsTable.firebaseUid eq firebaseUid }) {
            dto.notifyMarketing?.let { value -> it[notifyMarketing] = value }
            dto.notifyOrderUpdates?.let { value -> it[notifyOrderUpdates] = value }
        }
        updatedRows > 0
    }

    fun createForFirebaseUid(firebaseUid: String): Boolean = transaction {
        UserNotificationsTable.insertIgnore {
            it[UserNotificationsTable.firebaseUid] = firebaseUid
            // rely on DB defaults: notifyMarketing=false, notifyOrderUpdates=true
        }
        true
    }
}
