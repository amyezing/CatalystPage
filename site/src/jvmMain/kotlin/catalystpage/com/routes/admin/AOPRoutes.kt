package catalystpage.com.routes.admin

import admin.dto.PaymentStatusUpdateRequest
import admin.dto.PaymentStatusUpdateResponse
import catalystpage.com.service.admin.AdminPaymentService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminPaymentRoutes() {
    route("/payments") {
        route("/admin") {
            get("/summary") {
                val summaries = AdminPaymentService.getPaymentSummary()
                call.respond(summaries)

            }

            put("/status") {
                val payload = call.receive<PaymentStatusUpdateRequest>()
                val success = AdminPaymentService.updatePaymentStatus(payload.orderId, payload.status)
                call.respond(
                    if (success)
                        PaymentStatusUpdateResponse(true, "Status updated successfully")
                    else
                        HttpStatusCode.NotFound to PaymentStatusUpdateResponse(false, "Order not found")
                )
            }
        }
    }
}