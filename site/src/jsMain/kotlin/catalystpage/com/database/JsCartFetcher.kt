package catalystpage.com.database

import dto.CartItemDTO
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import model.CartCountDTO
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

class JsCartFetcher {

    suspend fun getCartItems(firebaseUid: String): List<CartItemDTO> {
        val response = window.fetch("http://localhost:8081/api/cart/$firebaseUid").await()
        return if (response.ok) {
            response.json().await().unsafeCast<Array<CartItemDTO>>().toList()
        } else emptyList()
    }

    suspend fun addToCart(item: CartItemDTO): CartItemDTO {
        val response = window.fetch("http://localhost:8081/api/cart", RequestInit(
            method = "POST",
            headers = Headers().apply { set("Content-Type", "application/json") },
            body = Json.encodeToString(CartItemDTO.serializer(), item)
        )).await()

        if (!response.ok) {
            throw Exception("HTTP ${response.status}: ${response.statusText}")
        }

        val text = response.text().await()
        return Json.decodeFromString(CartItemDTO.serializer(), text)
    }

    suspend fun updateQuantity(id: Int, quantity: Int): Boolean {
        val firebaseUid = window.localStorage.getItem("firebaseUserUID") ?: return false

        val item = CartItemDTO(
            id = id,
            firebaseUid = firebaseUid,
            quantity = quantity,
            productVariantId = null
        )

        val response = window.fetch("/api/cart/$id", RequestInit(
            method = "PUT",
            headers = Headers().apply { set("Content-Type", "application/json") },
            body = Json.encodeToString(CartItemDTO.serializer(), item)
        )).await()

        return response.ok
    }

    suspend fun getCartCount(firebaseUid: String): Int {
        val response = window.fetch("http://localhost:8081/api/cart/count/$firebaseUid").await()
        if (!response.ok) return 0

        val text = response.text().await() // get raw JSON string
        val data = Json.decodeFromString<CartCountDTO>(text) // decode into strongly-typed object
        return data.count
    }

    suspend fun deleteCartItem(id: Int): Boolean {
        val response = window.fetch("/api/cart/$id", RequestInit(method = "DELETE")).await()
        return response.ok
    }
}