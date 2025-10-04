package dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class PaymentDTO(
    val id: Int? = null,
    val orderId: Int,
    val amount: Double? = null,
    @SerialName("payment_method")val paymentMethod: PaymentMethod,
    val referenceNumber: String? = null,
    val status: PaymentStatus = PaymentStatus.PENDING,
    @SerialName("proof_image") val proofImage: String? = null,
    val createdAt: String? = null // or `Instant` if you handle date conversion
)

enum class PaymentMethod {
    Gcash, Bank_Transfer, Credit_Card, COD;

    companion object {
        fun fromDatabase(value: String): PaymentMethod = when (value) {
            "Gcash" -> Gcash
            "Bank Transfer" -> Bank_Transfer
            "Credit Card" -> Credit_Card
            "COD" -> COD
            else -> error("Unknown PaymentMethod: $value")
        }

        fun toDatabase(method: PaymentMethod): String = when (method) {
            Gcash -> "Gcash"
            Bank_Transfer -> "Bank Transfer"
            Credit_Card -> "Credit Card"
            COD -> "COD"
        }
    }
}

enum class PaymentStatus {
    PENDING, APPROVED, REJECTED;

    companion object {
        fun fromDatabase(value: String): PaymentStatus = when (value.uppercase()) {
            "PENDING" -> PENDING
            "APPROVED" -> APPROVED
            "REJECTED" -> REJECTED
            else -> error("Unknown PaymentStatus: $value")
        }

        fun toDatabase(status: PaymentStatus): String = status.name
    }
}

