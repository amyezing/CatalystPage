package catalystpage.com.service.admin

import admin.dto.AdminPaymentSummaryDTO
import catalystpage.com.entity.CartItemEntity
import catalystpage.com.entity.OrderEntity
import catalystpage.com.entity.UserEntity
import catalystpage.com.entity.admin.AdminShippingSummaryEntity
import catalystpage.com.model.CartItems
import catalystpage.com.model.Orders
import catalystpage.com.model.Payments
import catalystpage.com.model.Users
import catalystpage.com.model.admin.table.AdminPaymentSummary
import catalystpage.com.model.admin.table.AdminShippingSummaryTable
import catalystpage.com.service.EmailService
import dto.PaymentStatus
import model.OrderStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.text.DecimalFormat

object AdminPaymentService {
    fun getPaymentSummary(): List<AdminPaymentSummaryDTO> = transaction {
        AdminPaymentSummary.selectAll().map { row ->
            AdminPaymentSummaryDTO(
                orderId = row[AdminPaymentSummary.orderId],
                usersName = row[AdminPaymentSummary.usersName] ?: "Unknown",
                amount = row[AdminPaymentSummary.amount].toDouble(),
                paymentMethod = row[AdminPaymentSummary.paymentMethod],
                proofImage = row[AdminPaymentSummary.proofImage],
                status = row[AdminPaymentSummary.status] ?: "Pending",
                paymentDate = row[AdminPaymentSummary.paymentDate]?.toString() ?: "Unknown",
                statusUpdatedAt = row[AdminPaymentSummary.statusUpdatedAt]?.toString()
            )
        }
    }
    fun updatePaymentStatus(orderId: Int, status: String): Boolean = transaction {
        // ⚠️ Safety check: Only allow one APPROVED payment per order
        if (status.equals("APPROVED", ignoreCase = true)) {
            val existingApproved = Payments.select {
                (Payments.order eq orderId) and (Payments.status eq PaymentStatus.APPROVED)
            }.count()

            if (existingApproved > 0) {
                error("❌ A payment for this order has already been approved.")
            }
        }

        // Update payments table
        val updatedRows = Payments.update({ Payments.order eq orderId }) {
            it[Payments.status] = PaymentStatus.fromDatabase(status)
            it[Payments.statusUpdatedAt] = CurrentTimestamp()
        }

        // Update orders table to reflect payment status
        val newOrderStatus = when (status.uppercase()) {
            "APPROVED" -> OrderStatus.Paid
            "REJECTED" -> OrderStatus.Pending
            else -> OrderStatus.Pending
        }
        Orders.update({ Orders.id eq orderId }) {
            it[Orders.status] = newOrderStatus
        }

        // If approved, clear cart + notify user
        val orderEntity = OrderEntity.findById(orderId)
        val firebaseUid = orderEntity?.firebaseUid

        if (status.equals("APPROVED", ignoreCase = true)) {
            if (firebaseUid != null) {
                CartItemEntity.find { CartItems.firebaseUid eq firebaseUid }
                    .forEach(CartItemEntity::delete)
                println("🧹 Cart cleared for $firebaseUid after admin approval")
            } else {
                println("⚠️ firebaseUid is null for orderId=$orderId — cannot clear cart")
            }
        }

        // 📧 Notify user about payment status update
        if (orderEntity != null) {
            val user = UserEntity.find { Users.firebaseUid eq orderEntity.firebaseUid }.firstOrNull()

            // 🔹 Fetch shipping info (if any)
            val shipping = AdminShippingSummaryEntity.find {
                AdminShippingSummaryTable.orderId eq orderEntity.id.value
            }.firstOrNull()

            val df = DecimalFormat("#,##0.00")
            val orderTotal = orderEntity.totalPrice
            val shippingFee = shipping?.shippingFee ?: BigDecimal.ZERO
            val grandTotal = orderTotal + shippingFee

            user?.let {
                val subject = when (status.uppercase()) {
                    "APPROVED" -> "✅ Payment Approved for Order #${orderEntity.id.value}"
                    "REJECTED" -> "❌ Payment Rejected for Order #${orderEntity.id.value}"
                    else -> "ℹ️ Payment Update for Order #${orderEntity.id.value}"
                }

                val body = """
Hi ${it.name ?: "Customer"},

Thank you for your purchase with Catalyst! 
Your payment for Order #${orderEntity.id.value} has been successfully processed 
and marked as $status.

Order Summary:
- Order Total: ₱${df.format(orderTotal)}
- Shipping Fee: ₱${df.format(shippingFee)}
- Grand Total Paid: ₱${df.format(grandTotal)}

Current Order Status: $newOrderStatus

Our support team is here to assist you every step of the way until your order reaches your hands.  
If you have any questions, concerns, or feedback, we warmly welcome them  
your input helps us improve and serve you better.

You can check “My Orders” for your Tracking Number and electronic invoice copy.

Once again, thank you for choosing Catalyst. Enjoy your drinks, 
and we look forward to serving you again soon!

Warm regards,  
– Catalyst Team
""".trimIndent()

                EmailService.sendMail(
                    to = it.email ?: return@let,
                    subject = subject,
                    body = body
                )
            }
        }

        updatedRows > 0
    }


}