package catalystpage.com.routes

import catalystpage.com.service.LabelService
import dto.LabelDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.labelRoutes() {
    route("/labels") {
        get {
            call.respond(LabelService.getAll())
        }
        post {
            val label = call.receive<LabelDTO>()
            call.respond(HttpStatusCode.Created, LabelService.add(label))
        }
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val label = call.receive<LabelDTO>()
            val updated = LabelService.update(id, label)
                ?: return@put call.respond(HttpStatusCode.NotFound)
            call.respond(updated)
        }
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            LabelService.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}