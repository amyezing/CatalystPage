package catalystpage.com.routes

import catalystpage.com.service.NotificationService
import dto.UpdateSettingsDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notificationRoutes() {

    route("/notifications") {

        get("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing firebaseUid")
                return@get
            }

            val notification = NotificationService.getByFirebaseUid(firebaseUid)
            if (notification == null) {
                call.respond(HttpStatusCode.NotFound, "Notification settings not found")
            } else {
                call.respond(notification)
            }
        }

        // PUT /api/notifications/{firebaseUid}
        put("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing firebaseUid")
                return@put
            }

            val dto = call.receive<UpdateSettingsDTO>()
            val ok = NotificationService.updateSettingsByFirebaseUid(firebaseUid, dto)
            if (ok) {
                call.respond(HttpStatusCode.OK, "Notification settings updated")
            } else {
                call.respond(HttpStatusCode.NotFound, "Failed to update notification settings")
            }
        }

        // POST /api/notifications/{firebaseUid} - create default settings
        post("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing firebaseUid")
                return@post
            }

            val created = NotificationService.createForFirebaseUid(firebaseUid)
            if (created) {
                call.respond(HttpStatusCode.Created, "Notification settings created")
            } else {
                call.respond(HttpStatusCode.Conflict, "Failed to create notification settings")
            }
        }

    }
}

