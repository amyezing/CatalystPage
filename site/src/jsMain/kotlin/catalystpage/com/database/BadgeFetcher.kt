package catalystpage.com.database

import catalystpage.com.util.Constants
import dto.BadgeDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object BadgeFetcher {
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

    suspend fun fetchUserBadges(firebaseUid: String): List<BadgeDTO> {
        return client.get("api/badges/$firebaseUid").body()
    }

    // POST /badges/{firebaseUid}/unlock/{badgeId}
    suspend fun unlockBadge(firebaseUid: String, badgeId: String): Boolean {
        val response = client.post("api/badges/$firebaseUid/unlock/$badgeId")
        return response.status.isSuccess()
    }
    suspend fun unlockBadgesByPoints(firebaseUid: String): Boolean {
        val response = client.post("api/badges/$firebaseUid/unlock-by-points")
        return response.status.isSuccess()
    }


}