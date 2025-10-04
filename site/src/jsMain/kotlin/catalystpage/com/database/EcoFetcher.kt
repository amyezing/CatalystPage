package catalystpage.com.database

import dto.community.CommunityProgressDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import model.*

object EcoFetcher {

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

    suspend fun postRecycle(userId: Int, bottles: Int, zoneId: Int? = null): Boolean {
        return try {
            val response = client.post("api/eco/recycle") {
                contentType(ContentType.Application.Json)
                setBody(RecycleRequest(userId, bottles, zoneId))
            }

            if (response.status.isSuccess()) {
                val result = response.body<RecycleResponse>()
                console.log("♻️ Recycle response:", result)
                true
            } else {
                console.error("❌ Recycle failed: ${response.status}")
                false
            }
        } catch (e: Exception) {
            console.error("🔥 Failed to post recycle:", e)
            false
        }
    }



    suspend fun getCommunityTotal(monthYear: String? = null): Int {
        return try {
            val url = if (monthYear != null) {
                "api/eco/community-total?monthYear=$monthYear"
            } else {
                "api/eco/community-total"
            }

            val response: CommunityTotalResponse = client.get(url).body()
            response.totalBottles
        } catch (e: Exception) {
            console.error("Failed to fetch community total:", e)
            0
        }
    }

    suspend fun getCommunityLifetime(): Int {
        return try {
            val response: CommunityLifetimeResponse = client.get("api/eco/community/lifetime").body()
            response.lifetimeTotal
        } catch (e: Exception) {
            console.error("Failed to fetch community lifetime:", e)
            0
        }
    }

    suspend fun getUserTotal(userId: Int, monthYear: String? = null): Int {
        val url = if (monthYear != null) {
            "api/eco/user-total/$userId?monthYear=$monthYear"
        } else {
            "api/eco/user-total/$userId"
        }

        val response: Map<String, Int> = client.get(url).body()
        return response["userBottles"] ?: 0
    }
    suspend fun getUserProgress(userId: Int): UserProgressResponse? {
        return try {
            val response: UserProgressResponse = client.get("api/eco/user-progress/$userId").body()
            response
        } catch (e: Exception) {
            console.error("Failed to fetch user progress:", e)
            null
        }
    }

    suspend fun getTopZone(monthYear: String? = null): Pair<String, Int> = try {
        val url = if (monthYear != null) "api/eco/top-zone?monthYear=$monthYear" else "api/eco/top-zone"
        val response: TopZoneResponse = client.get(url).body()
        response.zoneName to response.bottles
    } catch (e: Exception) {
        console.error("Failed to fetch top zone:", e)
        "N/A" to 0
    }
}

