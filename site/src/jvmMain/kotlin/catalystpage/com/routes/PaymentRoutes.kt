package catalystpage.com.routes

import catalystpage.com.entity.OrderEntity
import catalystpage.com.entity.PaymentEntity
import catalystpage.com.service.PaymentService
import dto.PaymentMethod
import dto.PaymentStatus
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.math.BigDecimal
import java.util.*
import kotlinx.io.readByteArray

fun Route.paymentRoutes() {
    route("/payments") {

        post("/submit") {
            try {
                val multipart = call.receiveMultipart()
                var orderId: Int? = null
                var amount: Double? = null
                var method: PaymentMethod? = null
                var reference: String? = null
                var imageUrl: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "orderId" -> orderId = part.value.toIntOrNull()
                                "amount" -> amount = part.value.toDoubleOrNull()
                                "method" -> method = PaymentMethod.entries.firstOrNull {
                                    it.name.equals(part.value, ignoreCase = true)
                                } ?: throw IllegalArgumentException("Unknown payment method: ${part.value}")
                                "reference" -> reference = part.value
                            }
                        }

                        is PartData.FileItem -> {
                            val extension = File(part.originalFileName ?: "proof.jpg").extension
                            val fileName = "proof_${UUID.randomUUID()}.$extension"
                            val fileBytes = part.provider().readRemaining().readByteArray()

                            // ✅ Upload directly to GCS via PaymentService
                            imageUrl = PaymentService.uploadProof(fileName, fileBytes)
                        }

                        else -> Unit
                    }
                    part.dispose()
                }

                if (orderId == null || method == null || imageUrl == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing required fields.")
                    return@post
                }

                // Save payment in DB
                val payment = transaction {
                    val orderEntity = OrderEntity.findById(orderId!!)
                        ?: error("Order not found.")

                    val paymentEntity = PaymentEntity.new {
                        this.order = orderEntity
                        this.amount = BigDecimal.valueOf(amount ?: 0.0)
                        this.paymentMethod = method!!
                        this.referenceNumber = reference
                        this.status = PaymentStatus.PENDING
                        this.proofImage = imageUrl
                    }

                    paymentEntity.toDTO()
                }

                call.respond(HttpStatusCode.OK, payment)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }


        get("/status") {
            val orderId = call.request.queryParameters["orderId"]?.toIntOrNull()
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing orderId"))
                return@get
            }

            val payment = PaymentService.getPaymentByOrderId(orderId)
            if (payment != null) {
                call.respond(mapOf("status" to payment.status.name))
            } else {
                call.respond(mapOf("status" to "NONE"))
            }
        }

        get("/status") {
            val orderId = call.request.queryParameters["orderId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing orderId")

            val status: String? = transaction {
                var result: String? = null
                exec(
                    """
            SELECT status 
            FROM admin_payment_summary 
            WHERE order_id = $orderId 
            ORDER BY status_updated_at DESC 
            LIMIT 1
            """.trimIndent()
                ) { rs ->
                    if (rs.next()) {
                        result = rs.getString("status")
                    }
                }
                result
            }

            call.respond(mapOf("status" to (status ?: "Pending")))
        }
    }
}