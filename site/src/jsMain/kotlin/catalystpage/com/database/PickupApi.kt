package catalystpage.com.database

import catalystpage.com.util.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import model.CreatePickupRequest
import model.PickupDTO

object PickupApi {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun createPickup(orderId: Int, phoneNumber: String): PickupDTO? {
        val request = CreatePickupRequest(orderId, phoneNumber)
        return try {
            client.post {
                url {
                    protocol = if (Constants.PORT == 443) URLProtocol.HTTPS else URLProtocol.HTTP
                    host = Constants.HOST
                    port = Constants.PORT
                    encodedPath = "/api/pickups"
                }
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            console.error("❌ Error creating pickup: ${e.message}")
            null
        }
    }
}