package catalystpage.com.database

import dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object CheckoutService {


    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8081
            }
        }
    }


    suspend fun checkout(
        firebaseUid: String,
        address: String,
        items: List<CartItemDTO>
    ): Int? {
        val request = CheckoutRequest(
            firebaseUid = firebaseUid,
            address = address,
            items = items,
            courier = null
        )

        return try {
            val response = client.post("/api/orders/checkout") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val json = JSON.parse<dynamic>(response.bodyAsText())
                val orderId = json.orderId as Int
                println("✅ Checkout success: Order ID = $orderId")
                orderId
            } else {
                println("Checkout failed: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("Exception during checkout: ${e.message}")
            null
        }
    }

    suspend fun getPendingOrder(firebaseUid: String): Int? {
        return try {
            val response = client.get("/api/orders/pending") {
                parameter("uid", firebaseUid)
            }

            if (response.status.isSuccess()) {
                val json = JSON.parse<dynamic>(response.bodyAsText())
                json.orderId as Int
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to fetch pending order: ${e.message}")
            null
        }
    }


    suspend fun getCartItemsByOrderId(orderId: Int): List<CartItemDTO> {
        return try {
            val response = client.get("/api/orders/$orderId/items")

            if (response.status.isSuccess()) {
                response.body()
            } else {
                println("❌ Failed to fetch cart items for order $orderId: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            println("🚨 Exception during cart fetch: ${e.message}")
            emptyList()
        }
    }

    suspend fun getProductInfoByVariantId(variantId: Int): ProductDTO {
        return try {
            client.get("/api/product-variants/product-from-variant/$variantId").body()
        } catch (e: Exception) {
            println("🚨 Failed to fetch product by variant ID $variantId: ${e.message}")
            throw e
        }
    }

    suspend fun cancelOrder(orderId: Int, onComplete: () -> Unit) {
        try {
            val response = client.post("/api/orders/$orderId/cancel")
            if (response.status.isSuccess()) {
                println("🗑️ Order $orderId successfully cancelled.")
                onComplete()
            } else {
                println("❌ Failed to cancel order $orderId: ${response.status}")
            }
        } catch (e: Exception) {
            println("🚨 Exception while cancelling order $orderId: ${e.message}")
        }
    }

    suspend fun getShippingDetails(orderId: Int): String {
        return try {
            val response = client.get("/api/orders/$orderId/shipping")
            if (response.status.isSuccess()) {
                val json = JSON.parse<dynamic>(response.bodyAsText())
                json.address as String
            } else {
                println("❌ Failed to get shipping: ${response.status}")
                ""
            }
        } catch (e: Exception) {
            println("🚨 Exception while fetching shipping: ${e.message}")
            ""
        }
    }

    suspend fun getUserOrders(firebaseUid: String): List<OrderDTO> {
        return try {
            val response = client.get("/api/orders/user") {
                parameter("uid", firebaseUid)
            }

            if (response.status.isSuccess()) {
                response.body()
            } else {
                println("❌ Failed to fetch orders: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            println("🚨 Exception fetching orders: ${e.message}")
            emptyList()
        }
    }
    suspend fun fetchPaymentStatus(orderId: Int): String {
        return try {
            val response = client.get("/api/payments/status") {
                parameter("orderId", orderId)
            }

            if (response.status.isSuccess()) {
                response.body<Map<String, String>>()["status"] ?: "Pending"
            } else {
                println("❌ Failed to fetch payment status for order $orderId: ${response.status}")
                "Pending"
            }
        } catch (e: Exception) {
            println("🚨 Exception fetching payment status: ${e.message}")
            "Pending"
        }
    }

}