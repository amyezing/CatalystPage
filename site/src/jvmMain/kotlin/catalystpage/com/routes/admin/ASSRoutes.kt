package catalystpage.com.routes.admin

import admin.dto.ShippingDTO
import admin.dto.ShippingSummary
import catalystpage.com.service.admin.AdminShippingService
import dto.ShippingDetailsDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminShippingRoutes() {

    route("/admin/shipping") {

        get {
            call.respond(AdminShippingService.getAll())
        }
        post("/migrate") {
            AdminShippingService.migrateShippingDetailsToAdminShippingSummary()
            call.respond(HttpStatusCode.OK, "Migration completed")
        }

        get("{orderId}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing orderId")
                return@get
            }

            val shipping = AdminShippingService.getByOrderId(orderId)
            if (shipping == null) {
                call.respond(HttpStatusCode.NotFound, "Shipping details not found")
            } else {
                call.respond(shipping)
            }
        }

        put("{orderId}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull() ?: error("Invalid orderId")
            val dto = call.receive<ShippingDTO>()   // instead of ShippingDetailsDTO
            val result = AdminShippingService.updateByOrderId(orderId, dto)
            call.respond(result)
        }
        get("/is-ready/{orderId}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid orderId")
                return@get
            }

            val summary = AdminShippingService.getByOrderId(orderId)
            if (summary != null && summary.status == ShippingSummary.ready) {
                call.respond(HttpStatusCode.OK, mapOf("ready" to true))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("ready" to false))
            }
        }

        post {
            val dto = call.receive<ShippingDTO>()
            try {
                val created = AdminShippingService.create(dto)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Error creating shipping")
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing ID")
                return@put
            }

            val dto = call.receive<ShippingDTO>()
            val updated = AdminShippingService.update(id, dto)

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, "Shipping not found")
            } else {
                call.respond(updated)
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or missing ID")
                return@delete
            }

            val deleted = AdminShippingService.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Shipping not found")
            }
        }
    }
}