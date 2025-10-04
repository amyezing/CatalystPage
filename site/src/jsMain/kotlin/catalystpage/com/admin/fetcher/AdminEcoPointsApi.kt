package catalystpage.com.admin.fetcher

import catalystpage.com.util.Constants
import dto.EcoPointTransactionDTO
import dto.UserEcoPointsDTO
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
import model.AddPointsRequest

object AdminEcoPointsApi {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(json)
        }

        defaultRequest {
            url {
                protocol = if (Constants.PORT == 443) URLProtocol.HTTPS else URLProtocol.HTTP
                host = Constants.HOST
                port = Constants.PORT
            }
        }
    }

    // Get all transactions for a user
    suspend fun getUserEcoPoints(userId: Int): UserEcoPointsDTO {
        return client.get("/api/admin/users/$userId/eco-points").body()
    }

    // 📌 Get all transactions for a user
    suspend fun getUserTransactions(userId: Int): List<EcoPointTransactionDTO> {
        val response: String = client.get("/api/admin/users/$userId/eco-point-transactions").bodyAsText()
        return json.decodeFromString(response)
    }

    // 📌 Add points to a user
    suspend fun addPoints(userId: Int, points: Int, reason: String = "recycling_scan") {
        client.post("/api/admin/users/$userId/eco-points/add") {
            contentType(ContentType.Application.Json)
            setBody(AddPointsRequest(points, reason))
        }
    }

}