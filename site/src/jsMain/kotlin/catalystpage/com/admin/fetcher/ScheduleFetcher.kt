package catalystpage.com.admin.fetcher

import admin.dto.RecyclingScheduleDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ScheduleFetcher {

    private const val BASE = "api/schedule"

    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
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

    /** Fetch all schedules */
    suspend fun fetchAll(): List<RecyclingScheduleDTO> =
        client.get(BASE).body()

    /** Fetch schedules for a specific zone */
    suspend fun fetchByZone(zoneId: Int): List<RecyclingScheduleDTO> =
        client.get("$BASE/$zoneId").body()

    /** Add a new schedule */
    suspend fun add(schedule: RecyclingScheduleDTO): RecyclingScheduleDTO =
        client.post(BASE) {
            contentType(ContentType.Application.Json)
            setBody(schedule)
        }.body()

    /** Update an existing schedule */
    suspend fun update(id: Int, schedule: RecyclingScheduleDTO): Boolean =
        client.put("$BASE/$id") {
            contentType(ContentType.Application.Json)
            setBody(schedule)
        }.status.isSuccess()

    /** Delete a schedule */
    suspend fun delete(id: Int): Boolean =
        client.delete("$BASE/$id").status.isSuccess()
}
