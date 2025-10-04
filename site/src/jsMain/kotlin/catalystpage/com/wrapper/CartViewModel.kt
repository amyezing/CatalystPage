package catalystpage.com.wrapper

import androidx.compose.runtime.*
import catalystpage.com.util.Constants
import dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class CartViewModel {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
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

    var cartItems by mutableStateOf<List<CartItemUI>>(emptyList())
        private set

    private var _cartCount = mutableStateOf(0)
    val cartCount: State<Int> get() = _cartCount

    private fun updateCartCount() {
        _cartCount.value = cartItems.sumOf { it.quantity }
        println("🟢 Cart count updated: ${_cartCount.value}")
    }



    suspend fun loadCartItems(firebaseUid: String) {
        val response: List<CartItemDTO> = client.post("/api/cart/fetch") {
            contentType(ContentType.Application.Json)
            setBody("""{ "firebase_uid": "$firebaseUid" }""")
        }.body()

        cartItems = response.map { item ->
            if (item.productVariantId != null) {
                val product = getProductInfoByVariantId(item.productVariantId)
                val variant = product.variants.find { it.id == item.productVariantId }

                CartItemUI(
                    id = item.id ?: error("CartItemDTO.id is null"),
                    quantity = item.quantity,
                    title = product.name,
                    imageUrl = product.imageUrl ?: "",
                    packSize = variant?.quantity ?: 0,
                    packPrice = variant?.price?.toInt() ?: 0,
                    productVariantId = item.productVariantId,
                    isProduct = true
                )
            } else {
                error("Invalid cart item: missing productVariantId")
            }
        }

        updateCartCount()
    }
    //update 7/11/25
    suspend fun updateQuantity(id: Int, quantity: Int) {
        client.put("/api/cart/$id") {
            contentType(ContentType.Application.Json)
            setBody(QuantityUpdateDTO(quantity))
        }
        cartItems = cartItems.map { if (it.id == id) it.copy(quantity = quantity) else it }
        updateCartCount()

    }


    suspend fun removeItem(id: Int) {
        client.delete("/api/cart/$id")
        cartItems = cartItems.filter { it.id != id }
        updateCartCount()

    }


    private suspend fun getProductInfoByVariantId(variantId: Int): ProductDTO {
        return try {
            client.get("/api/product-variants/product-from-variant/$variantId").body()
        } catch (e: Exception) {
            println("❌ Failed to fetch product: $e")
            ProductDTO(
                id = -1,
                name = "Unknown Product",
                imageUrl = "",
                description = "",
                type = ProductType.SINGLE,
                price = 0.0,
                variants = emptyList()
            )
        }
    }

    suspend fun clearCart(firebaseUid: String) {
        try {
            client.delete("/api/cart/all/$firebaseUid")
            cartItems = emptyList()
            updateCartCount()
            println("🧹 Cart cleared for $firebaseUid")
        } catch (e: Exception) {
            println("❌ Failed to clear cart: ${e.message}")
        }
    }

    suspend fun checkout(firebaseUid: String, address: String, items: List<CartItemDTO>): Int? {
        return try {
            val request = CheckoutRequest(
                firebaseUid = firebaseUid,
                address = address,
                items = items
            )

            val response: Map<String, Int> = client.post("/api/checkout") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            response["orderId"]
        } catch (e: Exception) {
            println("❌ Checkout failed: ${e.message}")
            null
        }
    }

}

fun CartViewModel.getQuantityAsState(itemId: Int): State<Int> {
    return derivedStateOf {
        cartItems.find { it.id == itemId }?.quantity ?: 0
    }
}