package catalystpage.com.routes.admin

import catalystpage.com.db.EnvConfig
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminEmails() {
    get("/admin-emails") {
        call.respond(mapOf("emails" to EnvConfig.adminEmails))
    }

}