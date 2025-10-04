package catalystpage.com.routes.admin

import admin.dto.RecyclingScheduleDTO
import catalystpage.com.service.admin.RecyclingScheduleService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.recyclingScheduleRoutes() {
    route("/schedule") {
        get {
            call.respond(RecyclingScheduleService.getAll())
        }
        get("/{zoneId}") {
            val zoneId = call.parameters["zoneId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid zoneId")
            call.respond(RecyclingScheduleService.getByZone(zoneId))
        }
        post {
            val schedule = call.receive<RecyclingScheduleDTO>()
            call.respond(RecyclingScheduleService.add(schedule))
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")
            val schedule = call.receive<RecyclingScheduleDTO>()
            if (RecyclingScheduleService.update(id, schedule))
                call.respond(mapOf("success" to true))
            else call.respond(HttpStatusCode.NotFound, "Not found")
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")
            if (RecyclingScheduleService.delete(id))
                call.respond(mapOf("success" to true))
            else call.respond(HttpStatusCode.NotFound, "Not found")
        }
    }
}