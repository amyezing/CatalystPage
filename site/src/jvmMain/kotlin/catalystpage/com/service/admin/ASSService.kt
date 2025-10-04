package catalystpage.com.service.admin

import admin.dto.ShippingDTO
import admin.dto.ShippingSummary
import catalystpage.com.entity.OrderEntity
import catalystpage.com.entity.ShippingDetailsEntity
import catalystpage.com.entity.UserEntity
import catalystpage.com.entity.admin.AdminOrderSummaryEntity
import catalystpage.com.entity.admin.AdminShippingSummaryEntity
import catalystpage.com.model.Users
import catalystpage.com.model.admin.table.AdminShippingSummaryTable
import catalystpage.com.model.admin.table.toDTO
import catalystpage.com.service.EmailService
import catalystpage.com.util.ShippingNotifier
import catalystpage.com.util.mapShippingStatusToSummary
import dto.ShippingDetailsDTO
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime

object AdminShippingService {
    fun getAll(): List<ShippingDTO> = transaction {
        AdminShippingSummaryEntity.all().map { it.toDTO() }
    }

    fun getByOrderId(orderId: Int): ShippingDTO? = transaction {
        AdminShippingSummaryEntity.find { AdminShippingSummaryTable.orderId eq orderId }
            .firstOrNull()?.toDTO()
    }

    fun create(shipping: ShippingDTO): ShippingDTO = transaction {
        val orderExists = AdminOrderSummaryEntity.findById(shipping.orderId)
            ?: error("Order with ID ${shipping.orderId} not found")

        val entity = AdminShippingSummaryEntity.new {
            orderId = shipping.orderId
            courier = shipping.courier
            scheduledDate = shipping.scheduledDate?.let { LocalDate.parse(it) }
            trackingNumber = shipping.trackingNumber
            shippingFee = BigDecimal.valueOf(shipping.shippingFee)
            notes = shipping.notes
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
            status = shipping.status
        }

        entity.toDTO()
    }

    fun update(id: Int, updated: ShippingDTO): ShippingDTO? = transaction {
        val entity = AdminShippingSummaryEntity.findById(id) ?: return@transaction null

        entity.apply {
            courier = updated.courier
            scheduledDate = updated.scheduledDate?.let { LocalDate.parse(it) }
            trackingNumber = updated.trackingNumber
            shippingFee = BigDecimal.valueOf(updated.shippingFee)
            notes = updated.notes
            updatedAt = LocalDateTime.now()

            status = if (!courier.isNullOrBlank() &&
                !trackingNumber.isNullOrBlank() &&
                shippingFee > BigDecimal.ZERO &&
                scheduledDate != null
            ) ShippingSummary.ready else ShippingSummary.pending
        }

        val order = AdminOrderSummaryEntity.findById(entity.orderId)
            ?: error("Order with ID ${entity.orderId} not found")

        order.totalPrice += entity.shippingFee

        entity.toDTO()
    }

    fun delete(id: Int): Boolean = transaction {
        AdminShippingSummaryEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    fun migrateShippingDetailsToAdminShippingSummary() = transaction {
        ShippingDetailsEntity.all().forEach { sd ->
            val existing = AdminShippingSummaryEntity.find {
                AdminShippingSummaryTable.orderId eq sd.order.id.value
            }.firstOrNull()

            if (existing == null) {
                AdminShippingSummaryEntity.new {
                    orderId = sd.order.id.value
                    courier = sd.courier ?: ""
                    trackingNumber = sd.trackingNumber ?: ""
                    status = ShippingSummary.pending
                    shippingFee = BigDecimal.ZERO
                    notes = sd.address
                    scheduledDate = null
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }
            }
        }
    }

    fun upsertAdminShippingSummaryFromUserInput(dto: ShippingDetailsDTO): ShippingDTO {
        return transaction {
            val existing = AdminShippingSummaryEntity.find {
                AdminShippingSummaryTable.orderId eq dto.orderId
            }.firstOrNull()

            val statusEnum = mapShippingStatusToSummary(dto.status)

            existing?.apply {
                courier = dto.courier ?: courier
                trackingNumber = dto.trackingNumber ?: trackingNumber
                status = statusEnum
                updatedAt = LocalDateTime.now()
            }?.toDTO()
                ?: AdminShippingSummaryEntity.new {
                    orderId = dto.orderId
                    courier = dto.courier ?: ""
                    trackingNumber = dto.trackingNumber ?: ""
                    status = statusEnum
                    shippingFee = BigDecimal.ZERO
                    notes = dto.address
                    scheduledDate = null
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }.toDTO()
        }
    }

    suspend fun updateByOrderId(orderId: Int, dto: ShippingDTO): ShippingDTO {
        // run DB work inside transaction
        val dtoResult = transaction {
            val entity = AdminShippingSummaryEntity.find {
                AdminShippingSummaryTable.orderId eq orderId
            }.firstOrNull() ?: throw IllegalStateException("Shipping summary not found for orderId=$orderId")

            // update fields
            entity.courier = dto.courier
            entity.trackingNumber = dto.trackingNumber
            entity.scheduledDate = dto.scheduledDate?.let { LocalDate.parse(it) }
            entity.shippingFee = BigDecimal.valueOf(dto.shippingFee)
            entity.status = dto.status
            entity.notes = dto.notes

            val order = OrderEntity.findById(orderId)
                ?: throw IllegalStateException("Order not found for orderId=$orderId")

            val user = UserEntity.find { Users.firebaseUid eq order.firebaseUid }.firstOrNull()

            user?.email?.takeIf { it.isNotBlank() }?.let { email ->
                val formattedFee = DecimalFormat("#,##0.00").format(entity.shippingFee)
                val address = entity.notes ?: "No address provided"

                val messageBody = if (entity.trackingNumber.isNullOrBlank()) {
                    // 📌 Stage 1: Fee assigned, waiting for payment
                    """
                Hi ${user.name ?: "Customer"},
                
                Your order #$orderId shipping fee has been set.
                
                Courier: ${entity.courier ?: "N/A"}
                Shipping Fee: ₱$formattedFee
                Status: ${entity.status}
                Shipping Address: $address
                
                Please settle the shipping fee to proceed with processing your order.
                
                Thank you for shopping with us!
                """.trimIndent()
                } else {
                    // 📌 Stage 2: Tracking provided, shipping in progress
                    """
                Hi ${user.name ?: "Customer"},
                
                Your order #$orderId is now on its way!
                
                Courier: ${entity.courier ?: "N/A"}
                Tracking Number: ${entity.trackingNumber}
                Scheduled Date: ${entity.scheduledDate ?: "TBA"}
                Shipping Address: $address
                
                Thank you for shopping with us!
                """.trimIndent()
                }

                EmailService.sendMail(
                    to = email,
                    subject = "Your order #$orderId shipping update",
                    body = messageBody
                )
            }

            entity.toDTO()
        }

        // 🔔 Notify WebSocket listeners (outside transaction)
        ShippingNotifier.notify(orderId, "Shipping updated for order #$orderId")

        return dtoResult
    }

}