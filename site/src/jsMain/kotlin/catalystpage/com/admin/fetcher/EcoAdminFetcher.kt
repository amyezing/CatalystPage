package catalystpage.com.admin.fetcher

import dto.community.ZoneDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import model.PendingRecyclingDTO

object EcoAdminFetcher {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8081
            }
        }
    }
    suspend fun getZones(): List<ZoneDTO> {
        return try {
            client.get("api/zones").body()
        } catch (e: Exception) {
            console.error("Failed to fetch zones:", e)
            emptyList()
        }
    }

    suspend fun getPendingRequests(): List<PendingRecyclingDTO> {
        return try {
            client.get("api/eco/admin/pending").body() // now body() knows it’s a List<PendingRecyclingRequestDTO>
        } catch (e: Exception) {
            console.error("Failed to fetch pending requests:", e)
            emptyList()
        }
    }
    suspend fun confirmRequest(id: Int, zoneId: Int?): Boolean {
        return try {
            val response = client.post("api/eco/admin/confirm/$id") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("zoneId" to zoneId))
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            console.error("Failed to confirm request:", e)
            false
        }
    }

    suspend fun rejectRequest(id: Int): Boolean {
        return try {
            val response = client.post("api/eco/admin/reject/$id")
            response.status.isSuccess()
        } catch (e: Exception) {
            console.error("Failed to reject request:", e)
            false
        }
    }
}
