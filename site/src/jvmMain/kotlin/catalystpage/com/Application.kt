package catalystpage.com

import catalystpage.com.db.DbConnection
import catalystpage.com.routes.*
import catalystpage.com.routes.admin.*
import catalystpage.com.service.BadgeService
import catalystpage.com.service.CartItemService
import catalystpage.com.service.UserEcoPointsService
import catalystpage.com.util.shippingWebSocket
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.websocket.*

fun main() {
    embeddedServer(Netty, port = 8081, module = Application::module).start(wait = true)
    println("Server running at http://localhost:8081")

}


fun Application.module() {

    DbConnection.connect()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })

    }

    install(io.ktor.server.plugins.cors.routing.CORS) {
        allowHost("localhost:8080", schemes = listOf("http"))
        allowCredentials = true

        allowHeader("X-Firebase-Uid")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        allowNonSimpleContentTypes = true

    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        route("/api") {
            adminEmails()
            adminOrderRoutes()
            adminPaymentRoutes()
            adminShippingRoutes()
            auditRoutes()
            authRoutes()
            badgeRoutes(BadgeService())
            cartRoutes(CartItemService())
            ecoAdminRoutes()
            ecoPointsRoutes()
            ecoRoutes()
            frontendConfigRoutes()
            labelRoutes()
            lowStockRoutes()
            notificationRoutes()
            orderRoutes()
            paymentRoutes()
            pickupRoutes()
            productRoutes()
            productVariantRoutes()
            recyclingScheduleRoutes()
            shippingRoutes()
            shippingWebSocket()
            userEcoPointsRoutes(UserEcoPointsService())
            userRoutes()
            zoneRoutes()


        }
        staticFiles("/uploads", File("D:/catalyst/uploads"))
    }
}

