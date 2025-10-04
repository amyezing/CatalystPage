package catalystpage.com.entity

import catalystpage.com.model.Payments
import dto.PaymentDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PaymentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PaymentEntity>(Payments)

    var order by OrderEntity referencedOn Payments.order
    var amount by Payments.amount
    var paymentMethod by Payments.paymentMethod
    var referenceNumber by Payments.referenceNumber
    var status by Payments.status
    var proofImage by Payments.proofImage
    var createdAt by Payments.createdAt

    fun toDTO() = PaymentDTO(
        id = id.value,
        orderId = order.id.value,
        amount = amount?.toDouble(),
        paymentMethod = paymentMethod,
        referenceNumber = referenceNumber,
        status = status,
        proofImage = proofImage,
        createdAt = createdAt.toString()
    )
}