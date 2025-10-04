package catalystpage.com.routes

import catalystpage.com.service.CartItemService
import dto.CartItemDTO
import dto.QuantityUpdateDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.cartRoutes(service: CartItemService) {
    route("/cart") {

        post {
            try {
                println("📥 POST /api/cart called")

                val bodyText = call.receiveText()
                println("📦 Raw body: $bodyText")

                val item = Json.decodeFromString<CartItemDTO>(bodyText)
                println("✅ Parsed DTO: $item")

                val savedItem = service.addToCart(item)
                call.respond(savedItem)

            } catch (e: Exception) {
                println("❌ Failed to add to cart: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Failed to parse CartItemDTO")
            }
        }

        // Existing GET route
        get("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing or invalid Firebase UID")

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

            val filterProductId = call.request.queryParameters["productId"]?.toIntOrNull()

            val items = service.getCartItemsByUser(
                firebaseUid = firebaseUid,
                page = page,
                limit = limit,
                productVariantId = filterProductId
            )

            call.respond(items)
        }

        get("/count/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing Firebase UID")

            val total = service.getCartItemsByUser(firebaseUid, page = 1, limit = 100)
                .sumOf { it.quantity }

            call.respond(mapOf("count" to total))
        }



        // PUT /api/cart/{id}
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid cart item ID")

            val body = call.receive<QuantityUpdateDTO>()
            val updated = service.updateQuantity(id, body.quantity)

            if (updated) call.respond(HttpStatusCode.OK, "Quantity updated")
            else call.respond(HttpStatusCode.NotFound, "Cart item not found")
        }

        // DELETE /api/cart/{id}
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing ID")

            service.deleteCartItem(id)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/fetch") {
            val body = call.receive<Map<String, String>>()
            val firebaseUid = body["firebase_uid"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing firebase_uid")

            val items = service.getCartItemsByUser(
                firebaseUid = firebaseUid,
                page = 1,
                limit = 100
            )

            println("🧾 Returning cart items: ${Json.encodeToString(items)}") // Add this
            call.respond(items)
        }


        delete("/all/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing Firebase UID")

            val deletedCount = service.deleteAllForUser(firebaseUid)
            call.respond(HttpStatusCode.OK, "🗑️ Deleted $deletedCount cart items for $firebaseUid")
        }

    }
}