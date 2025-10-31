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
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.websocket.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import model.HealthResponse

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    println("🚀 Starting Catalyst backend on port $port")

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    println("📦 APPLICATION MODULE LOADING")

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        })
    }

    // Start database connection async but don't block server startup
    var databaseReady = false


    install(io.ktor.server.plugins.cors.routing.CORS) {
        anyHost()
        allowHost("catalyst-frontend-184459898636.asia-southeast1.run.app", schemes = listOf("https"))
        allowHost("www.catalystbeveragemanufacturing.com", schemes = listOf("https"))
        allowHost("catalyst-frontend-c3h2bpneja-as.a.run.app", schemes = listOf("https"))
        allowHost("catalyst-backend-184459898636.asia-southeast1.run.app", schemes = listOf("https"))
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("127.0.0.1:8080", schemes = listOf("http"))

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

        get("/live") {
            call.respondText("ALIVE", status = HttpStatusCode.OK)
        }

        get("/") {
            call.respondText("Catalyst backend is running!")
        }

        get("/test-cors") {
            call.respondText("CORS test - backend is running")
        }

        get("/health") {
            call.respond(HealthResponse(
                status = "running",
                timestamp = System.currentTimeMillis(),
                database = if (DbConnection.isConnected()) "connected" else "offline"
            ))
        }

        // ✅ Add a readiness check for Cloud Run
        get("/ready") {
            if (DbConnection.isConnected()) {
                call.respondText("READY", status = HttpStatusCode.OK)
            } else {
                call.respondText("DATABASE_NOT_READY", status = HttpStatusCode.ServiceUnavailable)
            }
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
    launch {
        try {
            println("🔗 Attempting database connection...")
            DbConnection.connect()
            databaseReady = true
            println("✅ Database connected successfully")
        } catch (e: Exception) {
            println("❌ Database connection failed: ${e.message}")
        }
    }
}