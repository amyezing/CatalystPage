package catalystpage.com.routes

import catalystpage.com.model.community.Zones
import dto.community.ZoneDTO
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.zoneRoutes() {
    route("zones") {
        get {
            val zones = transaction {
                Zones.selectAll().map {
                    ZoneDTO(
                        id = it[Zones.id],
                        name = it[Zones.name],
                        description = it[Zones.description]
                    )
                }
            }
            call.respond(zones)
        }
    }
}
