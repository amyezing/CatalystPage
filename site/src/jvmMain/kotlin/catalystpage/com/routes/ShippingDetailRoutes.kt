package catalystpage.com.routes

import admin.dto.ShippingDTO
import catalystpage.com.service.ShippingDetailsService
import catalystpage.com.service.admin.AdminShippingService
import dto.ShippingDetailsDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.CreateShippingDetailsRequest
import model.UpdateShippingStatusRequest

fun Route.shippingRoutes() {

    route("/shipping") {

        get {
            val all = ShippingDetailsService.getAll()
            call.respond(all)
        }


        post {
            val dto = call.receive<ShippingDetailsDTO>()
            val result = ShippingDetailsService.upsertUserShippingDetails(dto)
            call.respond(result)
        }

        post("/upsert") {
            val dto = call.receive<ShippingDTO>()
            val result = AdminShippingService.updateByOrderId(dto.orderId, dto)
            call.respond(result)
        }

        get("/{orderId}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order ID")

            val detail = ShippingDetailsService.getByOrderId(orderId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Shipping detail not found")

            call.respond(detail)
        }

        post {
            val req = call.receive<CreateShippingDetailsRequest>()
            val created = ShippingDetailsService.create(
                orderId = req.orderId,
                address = req.address
            )
            call.respond(created)
        }

        post("/update-status") {
            val req = call.receive<UpdateShippingStatusRequest>()

            val updated = ShippingDetailsService.updateStatus(
                orderId = req.orderId,
                status = req.status,
                trackingNumber = req.trackingNumber,
                courier = req.courier
            )

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, "Shipping detail not found")
            } else {
                call.respond(updated)
            }
        }


//          post("/upsert-summary") {
//            val dto = call.receive<ShippingDetailsDTO>()
//            val result = ShippingDetailsService.upsertAdminShippingSummaryFromUserInput(dto)
//            call.respond(result)
//        }
    }
}