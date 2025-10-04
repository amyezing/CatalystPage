package catalystpage.com.database

import dto.UpdateSettingsDTO
import dto.UserNotificationDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object NotificationFetcher {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
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

    suspend fun fetchNotificationSettings(firebaseUid: String): UserNotificationDTO? {
        return try {
            client.get("/api/notifications/$firebaseUid").body<UserNotificationDTO>()
        } catch (e: Throwable) {
            console.error("Error fetching notification settings:", e)
            null
        }
    }

    // PUT /api/notifications/{firebaseUid}
    suspend fun updateNotificationSettings(firebaseUid: String, dto: UpdateSettingsDTO): Boolean {
        return try {
            val response = client.put("/api/notifications/$firebaseUid") {
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            response.status.isSuccess()
        } catch (e: Throwable) {
            console.error("Error updating notification settings:", e)
            false
        }
    }

    // POST /api/notifications/{firebaseUid} - optional for creating default settings
    suspend fun createNotificationSettings(firebaseUid: String): Boolean {
        return try {
            val response = client.post("/api/notifications/$firebaseUid")
            response.status.isSuccess()
        } catch (e: Throwable) {
            console.error("Error creating notification settings:", e)
            false
        }
    }

}