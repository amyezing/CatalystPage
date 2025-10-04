package catalystpage.com.routes.admin


import catalystpage.com.service.admin.EcoAdminService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.ecoAdminRoutes() {
    route("eco/admin") {

        get("/pending") {
            try {
                val requests = EcoAdminService.getPendingRequests()
                call.respond(requests) // now Kotlinx Serialization can handle it
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        post("confirm/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")

            // Read zoneId from JSON body
            val body = call.receive<Map<String, Int?>>()
            val zoneId = body["zoneId"]

            val success = EcoAdminService.confirmRequest(id, zoneId)
            if (success) {
                call.respond(mapOf("success" to true))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("success" to false))
            }
        }

        post("reject/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")

            val success = EcoAdminService.rejectRequest(id)
            if (success) {
                call.respond(mapOf("success" to true))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("success" to false))
            }
        }
    }
}
