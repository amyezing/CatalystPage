package catalystpage.com.admin.fetcher

import catalystpage.com.util.Constants
import dto.LowStockItemDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

val stockApiClient = HttpClient {
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

suspend fun fetchLowStockReport(): List<LowStockItemDTO> {
    return try {
        stockApiClient.get("/api/admin/low-stock-items").body()
    } catch (e: Exception) {
        println("Error fetching low stock report: ${e.message}")
        emptyList()
    }
}

suspend fun updateStock(itemId: Int, newStock: Int, itemType: String): Boolean {
    return try {
        val response = stockApiClient.post("/api/admin/update-stock") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("itemId", JsonPrimitive(itemId))
                    put("newStock", JsonPrimitive(newStock))
                    put("itemType", JsonPrimitive(itemType))
                }
            )
        }
        response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        println("Failed to update stock: ${e.message}")
        false
    }
}