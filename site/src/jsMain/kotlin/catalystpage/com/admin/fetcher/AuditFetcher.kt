package catalystpage.com.admin.fetcher

import admin.dto.AuditLogDTO
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json

object AuditFetcher {
    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun getAll(): List<AuditLogDTO> {
        val response = window.fetch("http://localhost:8081/api/audit").await()
        if (!response.ok) {
            val text = response.text().await()
            throw Exception("Failed to fetch audit logs: ${response.status} $text")
        }
        val rawText = response.text().await()
        return jsonParser.decodeFromString(rawText)
    }
}