package catalystpage.com.service

import catalystpage.com.model.Pickups
import catalystpage.com.model.Pickups.status
import catalystpage.com.model.toPickupDTO
import model.PickupDTO
import model.PickupStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PickupService {

    fun createPickup(orderId: Int, phoneNumber: String): PickupDTO? {
        var dto: PickupDTO? = null
        transaction {
            // Insert a new pickup row
            val insertStatement = Pickups.insert { row ->
                row[Pickups.orderId] = orderId
                row[Pickups.phoneNumber] = phoneNumber
                row[Pickups.status] = PickupStatus.Pending
            }

            // Retrieve the generated id
            val generatedId = insertStatement[Pickups.id]

            // Fetch the full row as DTO
            dto = Pickups.select { Pickups.id eq generatedId }
                .singleOrNull()
                ?.toPickupDTO()
        }
        return dto
    }

    fun updateStatus(pickupId: Int, status: PickupStatus): Boolean {
        return transaction {
            Pickups.update({ Pickups.id eq pickupId }) { row ->
                row[Pickups.status] = status
                row[Pickups.updatedAt] = CurrentDateTime
            } > 0
        }
    }

    fun getByOrderId(orderId: Int): PickupDTO? {
        return transaction {
            Pickups.select { Pickups.orderId eq orderId }
                .singleOrNull()
                ?.toPickupDTO()
        }
    }
}
