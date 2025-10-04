package catalystpage.com.admin.fetcher

import admin.dto.AuditLogDTO
import catalystpage.com.util.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json

object AuditFetcher {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun getAll(): List<AuditLogDTO> {
        return try {
            client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = Constants.HOST
                    port = Constants.PORT
                    encodedPath = "/api/audit"
                }
            }.body()
        } catch (e: Exception) {
            console.error("❌ Failed to fetch audit logs: ${e.message}")
            emptyList()
        }
    }
}
