package catalystpage.com.admin.fetcher

import admin.dto.AdminOrderDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val orderApiClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }

    defaultRequest {
        url {
            protocol = URLProtocol.HTTP
            host = "localhost" // 🔁 Use window.location.hostname in production
            port = 8081
        }
    }

}

suspend fun fetchAdminOrderSummary(): List<AdminOrderDTO> {
    return try {
        orderApiClient.get("/api/orders/admin/summary").body()
    } catch (e: Exception) {
        console.error("❌ Failed to fetch admin order summary", e)
        emptyList()
    }
}

suspend fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
    return try {
        val response = orderApiClient.post("/api/orders/admin/$orderId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to newStatus)) // JSON: { "status": "Approved" }
        }
        response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        console.error("❌ Failed to update order status", e)
        false
    }
}

