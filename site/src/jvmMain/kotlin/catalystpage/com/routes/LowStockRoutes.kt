package catalystpage.com.routes

import admin.dto.UpdateStockRequest
import catalystpage.com.service.LowStockService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.lowStockRoutes() {
    route("/admin") {
        get("/low-stock-items") {
            val items = LowStockService.getAllLowStockItems()
            call.respond(items)
        }
        post("update-stock") {
            val request = call.receive<UpdateStockRequest>()

            val updated = LowStockService.updateStock(
                itemId = request.itemId,
                newStock = request.newStock,
                itemType = request.itemType
            )

            if (updated) {
                call.respond(HttpStatusCode.OK, "Stock updated")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Failed to update stock")
            }
        }
    }
}