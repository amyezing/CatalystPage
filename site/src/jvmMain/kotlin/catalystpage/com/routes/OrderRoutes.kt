package catalystpage.com.routes

import catalystpage.com.entity.ShippingDetailsEntity
import catalystpage.com.entity.toDTO
import catalystpage.com.model.ShippingDetails
import catalystpage.com.service.OrderService
import dto.CheckoutRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.orderRoutes() {
    route("/orders") {
        post("/checkout") {
            try {
                val request = call.receive<CheckoutRequest>()
                println("Incoming CheckoutRequest: $request")

                val order = OrderService.createOrder(request)
                call.respond(HttpStatusCode.OK, mapOf("orderId" to order.id.value))
            } catch (e: Exception) {
                e.printStackTrace() // This will print the full error and stack trace
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Checkout failed")
            }
        }

        get("/pending") {
            val uid = call.request.queryParameters["uid"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing uid")

            val order = OrderService.getPendingOrder(uid)
            if (order != null) {
                call.respond(mapOf("orderId" to order.id.value))
            } else {
                call.respond(mapOf("orderId" to null))
            }
        }

        get("/{id}/items") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or invalid order ID")

            try {
                val items = OrderService.getOrderItems(id)
                call.respond(items)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Failed to fetch order items")
            }
        }

        post("/{id}/cancel") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val cancelled = OrderService.cancelOrderById(id)

            if (cancelled) {
                println("Order $id cancelled.")
                call.respond(HttpStatusCode.OK)
            } else {
                println("Order $id could not be cancelled.")
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/{id}/shipping") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or invalid order ID")

            val shipping = transaction {
                ShippingDetailsEntity.find { ShippingDetails.order eq id }.firstOrNull()
            }

            if (shipping != null) {
                call.respond(mapOf(
                    "address" to shipping.address,
                    "courier" to shipping.courier,
                    "status" to shipping.status.name,
                    "trackingNumber" to shipping.trackingNumber,
                    "shippedAt" to shipping.shippedAt?.toString(),
                    "deliveredAt" to shipping.deliveredAt?.toString()
                ))
            } else {
                call.respond(HttpStatusCode.NotFound, "Shipping info not found")
            }

        }

        get("/user") {
            val uid = call.request.queryParameters["uid"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing uid")

            try {
                val orders = OrderService.getOrdersByUser(uid)
                call.respond(orders.map { it.toDTO() }) // Ensure your `OrderEntity` has a `toDTO()` extension
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Failed to fetch user orders")
            }
        }
    }

}