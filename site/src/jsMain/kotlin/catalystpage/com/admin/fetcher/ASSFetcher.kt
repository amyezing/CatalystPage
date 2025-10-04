package catalystpage.com.admin.fetcher

import admin.dto.ShippingDTO
import catalystpage.com.database.userShipping
import catalystpage.com.util.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

val adminShipping = HttpClient {
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

suspend fun fetchShippingList(scope: CoroutineScope, onResult: (List<ShippingDTO>) -> Unit) {
    try {
        val result = adminShipping.get("/api/admin/shipping").body<List<ShippingDTO>>()
        onResult(result)
    } catch (e: Exception) {
        console.error("Error fetching shipping list", e)
    }
}

suspend fun createShipping(dto: ShippingDTO) {
    adminShipping.post("/api/admin/shipping") {
        contentType(ContentType.Application.Json)
        setBody(dto)
    }
}

suspend fun deleteShipping(id: Int) {
    adminShipping.delete("/api/admin/shipping/$id")
}

suspend fun isShippingReady(orderId: Int): Boolean {
    return try {
        val result = adminShipping.get("/api/admin/shipping/is-ready/$orderId")
            .body<Map<String, Boolean>>()
        result["ready"] == true
    } catch (e: Exception) {
        console.error("Error checking shipping status", e)
        false
    }
}

suspend fun fetchShippingSummary(): List<ShippingDTO> {
    return try {
        adminShipping.get("/api/admin/shipping").body()
    } catch (e: Exception) {
        console.error("Error fetching shipping summary", e)
        emptyList()
    }
}


suspend fun updateShippingById(orderId: Int, dto: ShippingDTO): ShippingDTO {
    return userShipping.put("/api/admin/shipping/$orderId") {
        contentType(ContentType.Application.Json)
        setBody(dto)
    }.body()
}

suspend fun migrateShippingData() {
    try {
        adminShipping.post("/api/admin/shipping/migrate")
        println("Migration triggered successfully")
    } catch (e: Exception) {
        console.error("Error triggering migration", e)
    }
}

suspend fun fetchShippingSummaryByOrderId(orderId: Int): ShippingDTO? {
    return try {
        adminShipping.get("/api/admin/shipping/$orderId").body()
    } catch (e: Exception) {
        null
    }
}
