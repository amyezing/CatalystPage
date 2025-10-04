package catalystpage.com.util

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.shippingWebSocket() {
    webSocket("/ws/shipping/global") {
        println("🌍 Global shipping socket connected")
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    println("📩 Global message: ${frame.readText()}")
                }
            }
        } catch (e: Exception) {
            println("❌ WebSocket error (global): ${e.message}")
        } finally {
            println("🔒 Global shipping socket closed")
        }
    }

    // Order-specific socket
    webSocket("/ws/shipping/{orderId}") {
        val orderId = call.parameters["orderId"]?.toIntOrNull()
        if (orderId == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid orderId"))
            return@webSocket
        }

        println("📦 Order $orderId socket connected")
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    println("📩 Received from client (orderId=$orderId): ${frame.readText()}")
                }
            }
        } catch (e: Exception) {
            println("❌ WebSocket error (orderId=$orderId): ${e.message}")
        } finally {
            println("🔒 Order $orderId socket closed")
        }
    }
}