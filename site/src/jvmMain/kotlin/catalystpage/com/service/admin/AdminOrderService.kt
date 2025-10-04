package catalystpage.com.service.admin

import admin.dto.AdminOrderDTO
import catalystpage.com.entity.OrderEntity
import catalystpage.com.entity.UserEntity
import catalystpage.com.model.Users
import catalystpage.com.model.admin.table.AdminOrderSummary
import catalystpage.com.service.EmailService
import model.OrderStatus
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object AdminOrderService {
    fun getOrderSummary(): List<AdminOrderDTO> = transaction {
        AdminOrderSummary.selectAll().map { row ->
            AdminOrderDTO(
                id = row[AdminOrderSummary.orderId],
                firebaseUid = "N/A", //if firebaseUid isn't included in the view
                userEmail = row[AdminOrderSummary.usersEmail] ?: "N/A",
                totalPrice = row[AdminOrderSummary.totalPrice].toDouble(),
                status = (row[AdminOrderSummary.status] ?: "Unknown").toString(),
                createdAt = row[AdminOrderSummary.createdAt]?.toString() ?: "N/A",
                address = row[AdminOrderSummary.address]
            )
        }
    }

}