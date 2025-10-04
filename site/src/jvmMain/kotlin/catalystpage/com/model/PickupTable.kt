package catalystpage.com.model

import model.PickupDTO
import model.PickupStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Pickups : Table("pickups") {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id)
    val phoneNumber = varchar("phone_number", 50)
    val status = enumerationByName("status", 20, PickupStatus::class)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id, name = "PK_Pickups_ID") // declare primary key here
}

fun ResultRow.toPickupDTO() = PickupDTO(
    id = this[Pickups.id],
    orderId = this[Pickups.orderId],
    phoneNumber = this[Pickups.phoneNumber],
    status = this[Pickups.status],
    createdAt = this[Pickups.createdAt].toString(),
    updatedAt = this[Pickups.updatedAt].toString()
)