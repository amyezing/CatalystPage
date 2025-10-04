package catalystpage.com.routes.admin

import catalystpage.com.service.admin.AdminOrderService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminOrderRoutes() {
    route("/orders/admin") {
        get("/summary") {
            try {
                val summary = AdminOrderService.getOrderSummary()
                call.respond(summary)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Failed to fetch order summary")
            }
        }
    }
}