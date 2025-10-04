package catalystpage.com.routes

import catalystpage.com.model.UserEcoPoints
import catalystpage.com.service.EcoPointService
import dto.UserEcoPointsDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.AddPointsRequest
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.ecoPointsRoutes() {

    route("/admin/users/{id}") {

        // 📌 Get total points of a user
        get("/eco-points") {
            val userId = call.parameters["id"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val row = transaction {
                UserEcoPoints
                    .select { UserEcoPoints.userId eq userId }
                    .firstOrNull()
            }

            if (row == null) {
                call.respond(HttpStatusCode.NotFound, "User not found in eco points")
                return@get
            }

            val dto = UserEcoPointsDTO(
                userId = userId,
                points = row[UserEcoPoints.points],
                updatedAt = row[UserEcoPoints.updatedAt].toString()
            )

            call.respond(dto)
        }

        // 📌 Add points
        post("/eco-points/add") {
            val userId = call.parameters["id"]?.toIntOrNull()
            val body = call.receive<AddPointsRequest>() // { points: Int, reason: String? }

            if (userId == null || body.points <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Invalid input")
                return@post
            }

            EcoPointService.addPointsByUserId(
                userId,
                body.points,
                body.reason ?: "recycling_scan"
            )

            call.respond(HttpStatusCode.OK, mapOf("message" to "Points added"))
        }

        // 📌 Transactions history
        get("/eco-point-transactions") {
            val userId = call.parameters["id"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val transactions = EcoPointService.getTransactions(userId)
            call.respond(transactions)
        }
    }
}
