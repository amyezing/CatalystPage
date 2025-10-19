package catalystpage.com.database

import admin.dto.ShippingDTO
import catalystpage.com.util.Constants
import dto.ShippingDetailsDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val userShipping = HttpClient {
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


suspend fun upsertShippingDetail(dto: ShippingDetailsDTO): ShippingDetailsDTO? {
    return try {
        userShipping.post("/api/shipping") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    } catch (e: ClientRequestException) {
        println("Client error: ${e.response.status}")
        null
    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
        null
    }
}

suspend fun updateShippingById(orderId: Int, dto: ShippingDTO): ShippingDTO {
    return userShipping.put("/api/shipping/upsert") {
        contentType(ContentType.Application.Json)
        setBody(dto)
    }.body()
}


