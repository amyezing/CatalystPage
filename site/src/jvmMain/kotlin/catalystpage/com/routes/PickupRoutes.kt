package catalystpage.com.routes

import catalystpage.com.service.PickupService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.CreatePickupRequest
import model.UpdatePickupStatusRequest

fun Route.pickupRoutes() {

    route("/pickups") {

        // Create a pickup
        post("/create") {
            val request = call.receive<CreatePickupRequest>()
            val pickup = PickupService.createPickup(
                orderId = request.orderId,
                phoneNumber = request.phoneNumber
            )
            if (pickup != null) call.respond(HttpStatusCode.OK, pickup)
            else call.respond(HttpStatusCode.InternalServerError, "Failed to create pickup")
        }

        // Update pickup status
        post("/update-status") {
            val request = call.receive<UpdatePickupStatusRequest>()
            val success = PickupService.updateStatus(
                pickupId = request.pickupId,
                status = request.status
            )
            if (success) call.respond(HttpStatusCode.OK, "Status updated")
            else call.respond(HttpStatusCode.InternalServerError, "Failed to update status")
        }

        // Get pickup by order ID
        get("/order/{orderId}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid orderId")
                return@get
            }

            val pickup = PickupService.getByOrderId(orderId)
            if (pickup != null) call.respond(HttpStatusCode.OK, pickup)
            else call.respond(HttpStatusCode.NotFound, "Pickup not found")
        }
    }
}