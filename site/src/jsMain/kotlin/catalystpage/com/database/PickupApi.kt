package catalystpage.com.database

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import model.CreatePickupRequest
import model.PickupDTO

object PickupApi {
    private val client = HttpClient()

    suspend fun createPickup(orderId: Int, phoneNumber: String): PickupDTO? {
        val request = CreatePickupRequest(orderId, phoneNumber)
        return try {
            client.post("http://localhost:8080/api/pickups") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<PickupDTO>()
        } catch (e: Exception) {
            console.error("Error creating pickup: ${e.message}")
            null
        }
    }
}