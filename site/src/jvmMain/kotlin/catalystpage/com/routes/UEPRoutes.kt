package catalystpage.com.routes

import catalystpage.com.service.UserEcoPointsService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.AddPointsRequest

fun Route.userEcoPointsRoutes(service: UserEcoPointsService) {

    get("/users/{id}/eco-points") {
        val userId = call.parameters["id"]?.toIntOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
            return@get
        }
        val points = service.getPoints(userId)
        if (points == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
        } else {
            call.respond(points)
        }
    }

    post("/users/{id}/eco-points/add") {
        val userId = call.parameters["id"]?.toIntOrNull()
        val body = call.receive<AddPointsRequest>()
        if (userId == null || body.points <= 0) {
            call.respond(HttpStatusCode.BadRequest, "Invalid input")
            return@post
        }
        service.addPoints(userId, body.points)
        call.respond(HttpStatusCode.OK, mapOf("message" to "Points added"))
    }
}