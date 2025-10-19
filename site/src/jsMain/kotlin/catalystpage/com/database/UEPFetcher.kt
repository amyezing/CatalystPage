package catalystpage.com.database

import catalystpage.com.util.Constants
import dto.UserEcoPointsDTO
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object UEPFetcher {
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

    suspend fun fetchUserEcoPoints(userId: Int): UserEcoPointsDTO? {
        return try {
            val response: HttpResponse = client.get("/api/users/$userId/eco-points")
            if (response.status.isSuccess()) {
                Json.decodeFromString<UserEcoPointsDTO>(response.bodyAsText())
            } else {
                console.error("Error fetching eco points: ${response.status}")
                null
            }
        } catch (e: Throwable) {
            console.error("Error fetching eco points:", e)
            null
        }
    }

    suspend fun addUserEcoPoints(userId: Int, points: Int) {
        try {
            val response: HttpResponse = client.post("/api/users/$userId/eco-points/add") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(mapOf("points" to points)))
            }

            if (!response.status.isSuccess()) {
                console.error("Failed to add eco points")
            }
        } catch (e: Throwable) {
            console.error("Error adding eco points:", e)
        }
    }
}