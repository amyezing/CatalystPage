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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.websocket.*
import kotlinx.coroutines.launch

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    println("Starting server on port $port...")
    embeddedServer(Netty, host = "0.0.0.0", port = port, module = Application::module).start(wait = true)

}

fun Application.module() {
    launch {
        try {
            DbConnection.connect()
            println("Database connected")
        } catch (e: Exception) {
            println("Database connection failed: ${e.message}")
        }
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })
    }

    // ... rest of your configuration stays the same
    install(io.ktor.server.plugins.cors.routing.CORS) {
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Head)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-Firebase-Uid")
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowMethods)
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respondText("Catalyst backend is running!")
        }

        get("/test-cors") {
            call.respondText("CORS test - backend is running")
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "running",
                "timestamp" to System.currentTimeMillis(),
                "database" to if (DbConnection.isConnected()) "connected" else "offline"
            ))
        }

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
    }
}