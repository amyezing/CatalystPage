package catalystpage.com.util

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

object ShippingNotifier {
    private val sessions = mutableMapOf<Int, MutableList<DefaultWebSocketSession>>()

    fun register(orderId: Int, session: DefaultWebSocketSession) {
        sessions.computeIfAbsent(orderId) { mutableListOf() }.add(session)
    }

    suspend fun notify(orderId: Int, message: String) {
        sessions[orderId]?.forEach { session ->
            session.send(Frame.Text(message))
        }
    }

    fun unregister(orderId: Int, session: DefaultClientWebSocketSession) {
        sessions[orderId]?.remove(session)
    }
}