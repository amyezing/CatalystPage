package catalystpage.com.service

import catalystpage.com.entity.PaymentEntity
import catalystpage.com.model.Payments
import catalystpage.com.util.GcsService
import dto.PaymentDTO
import dto.PaymentStatus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction

object PaymentService {
    fun uploadProof(fileName: String, bytes: ByteArray): String {
        return GcsService.uploadProof("upload/$fileName", bytes)
    }

    fun getPaymentByOrderId(orderId: Int): PaymentDTO? = transaction {
        PaymentEntity.find { Payments.order eq orderId }
            .orderBy(Payments.createdAt to SortOrder.DESC)
            .firstOrNull()
            ?.toDTO()
    }

    fun updatePaymentStatus(paymentId: Int, newStatus: PaymentStatus): PaymentDTO? = transaction {
        val payment = PaymentEntity.findById(paymentId) ?: return@transaction null
        payment.status = newStatus
        payment.toDTO()
    }
}