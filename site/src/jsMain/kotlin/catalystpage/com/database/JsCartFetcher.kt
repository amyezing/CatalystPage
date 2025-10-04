package catalystpage.com.database

import catalystpage.com.util.Constants
import dto.CartItemDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import model.CartCountDTO
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

class JsCartFetcher {

    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            url {
                protocol = if (Constants.PORT == 443) URLProtocol.HTTPS else URLProtocol.HTTP
                host = Constants.HOST
                port = Constants.PORT
            }
        }
    }

    suspend fun getCartItems(firebaseUid: String): List<CartItemDTO> {
        return client.get("api/cart/$firebaseUid").body()
    }

    suspend fun addToCart(item: CartItemDTO): CartItemDTO {
        return client.post("api/cart") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }.body()
    }

    suspend fun updateQuantity(id: Int, quantity: Int): Boolean {
        val firebaseUid = window.localStorage.getItem("firebaseUserUID") ?: return false
        val item = CartItemDTO(
            id = id,
            firebaseUid = firebaseUid,
            quantity = quantity,
            productVariantId = null
        )

        val response = client.put("api/cart/$id") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }

        return response.status.isSuccess()
    }

    suspend fun getCartCount(firebaseUid: String): Int {
        val data: CartCountDTO = client.get("api/cart/count/$firebaseUid").body()
        return data.count
    }

    suspend fun deleteCartItem(id: Int): Boolean {
        val response = client.delete("api/cart/$id")
        return response.status.isSuccess()
    }
}