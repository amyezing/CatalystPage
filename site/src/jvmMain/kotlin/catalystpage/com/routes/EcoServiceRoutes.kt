package catalystpage.com.routes

import catalystpage.com.model.Users
import catalystpage.com.model.community.*
import catalystpage.com.service.EcoService
import catalystpage.com.service.EcoService.currentMonthYear
import catalystpage.com.service.admin.EcoAdminService
import dto.community.CommunityProgressDTO
import dto.community.RecyclingStatus
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.ecoRoutes() {
    route("/eco") {

        get("/user-total/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
            val monthYear = call.request.queryParameters["monthYear"] ?: currentMonthYear()

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid userId")
                return@get
            }

            val userBottles = transaction {
                UserRecycling
                    .slice(UserRecycling.bottles.sum())
                    .select {
                        (UserRecycling.userId eq userId) and
                                (UserRecycling.monthYear eq monthYear) and
                                (UserRecycling.status eq RecyclingStatus.CONFIRMED)
                    }
                    .singleOrNull()?.get(UserRecycling.bottles.sum()) ?: 0
            }

            call.respond(mapOf("userBottles" to userBottles))
        }

        get("/top-zone") {
            val (zone, bottles) = EcoService.getTopZone()
            call.respond(TopZoneResponse(zone, bottles))
        }

        post("recycle") {
            try {
                val request = call.receive<RecycleRequest>()
                println("♻️ Incoming recycle request: $request")

                // recordRecyclingRequest already uses transaction(), so call it directly
                val insertedId = EcoService.recordRecyclingRequest(
                    userId = request.userId,
                    bottles = request.bottles,
                    zoneId = request.zoneId
                )

                call.respond(HttpStatusCode.OK, RecycleResponse(insertedId, "PENDING"))
            } catch (e: Exception) {
                println("🔥 Error in /recycle: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }
        get("/user-progress/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid userId")
                return@get
            }

            val progress = EcoService.getUserProgress(userId)
            call.respond(progress)
        }



        get("/pending") {
            val pending = transaction {
                UserRecycling
                    .select { UserRecycling.status eq RecyclingStatus.PENDING }
                    .map {
                        mapOf(
                            "id" to it[UserRecycling.id],
                            "userId" to it[UserRecycling.userId],
                            "bottles" to it[UserRecycling.bottles],
                            "zoneId" to it[UserRecycling.zoneId],
                            "monthYear" to it[UserRecycling.monthYear],
                            "createdAt" to it[UserRecycling.createdAt].toString()
                        )
                    }
            }
            call.respond(pending)
        }

        post("confirm/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")

            // Receive zoneId correctly as nullable Int
            val body = call.receive<Map<String, Int?>>()
            val zoneId = body["zoneId"]

            val success = EcoAdminService.confirmRequest(id, zoneId)
            if (success) call.respond(mapOf("success" to true))
            else call.respond(HttpStatusCode.BadRequest, mapOf("success" to false))
        }

        get("/zones") {
            val zones = transaction { Zones.selectAll().map { it.toZoneDTO() } }
            call.respond(zones)
        }

        get("/zone/progress/{month}") {
            val month = call.parameters["month"] ?: EcoService.currentMonthYear()
            val data = transaction {
                (ZoneProgress innerJoin Zones).select { ZoneProgress.monthYear eq month }
                    .map { row ->
                        mapOf(
                            "zone" to row[Zones.name],
                            "total_bottles" to row[ZoneProgress.totalBottles]
                        )
                    }
            }
            call.respond(data)
        }

        get("/community/lifetime") {
            val lifetimeTotal = EcoService.getCommunityLifetimeTotal()
            call.respond(CommunityLifetimeResponse(lifetimeTotal))
        }

        get("/community-total") {
            val monthYear = call.request.queryParameters["monthYear"] ?: EcoService.currentMonthYear()
            val total = EcoService.getCommunityTotal(monthYear)
            call.respond(CommunityTotalResponse(monthYear, total))
        }

        get("/community/progress/{month?}") {
            val month = call.parameters["month"] ?: EcoService.currentMonthYear()
            val cp = transaction { CommunityProgress.select { CommunityProgress.monthYear eq month }.singleOrNull() }
            val dto = cp?.toCommunityProgressDTO() ?: CommunityProgressDTO(month, 0)
            call.respond(dto)
        }
    }
}