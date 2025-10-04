package catalystpage.com.routes.admin

import catalystpage.com.service.admin.AuditService
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.text.get

fun Route.auditRoutes() {
    route("/audit") {
        get {
            val audits = AuditService.getAll()
            call.respond(audits)
        }
    }
}