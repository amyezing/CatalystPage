package catalystpage.com.routes

import catalystpage.com.service.BadgeService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.badgeRoutes(badgeService: BadgeService) {
    route("/badges") {
        // fetch all badges
        get {
            call.respond(badgeService.getAllBadges())
        }

        // fetch user badges (firebaseUid provided)
        get("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid != null) {
                call.respond(badgeService.getUserBadges(firebaseUid))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing firebaseUid")
            }
        }

        // unlock a badge for a user
        post("/{firebaseUid}/unlock/{badgeId}") {
            val firebaseUid = call.parameters["firebaseUid"]
            val badgeId = call.parameters["badgeId"]
            if (firebaseUid != null && badgeId != null) {
                val success = badgeService.unlockBadge(firebaseUid, badgeId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Badge unlocked")
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }
        }
        post("/{firebaseUid}/unlock-by-points") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid != null) {
                badgeService.unlockBadgesByPoints(firebaseUid)
                call.respond(HttpStatusCode.OK, "Checked and unlocked eligible badges")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid firebaseUid")
            }
        }
    }
}