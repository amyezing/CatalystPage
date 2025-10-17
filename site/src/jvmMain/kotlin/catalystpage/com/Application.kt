package catalystpage.com

import catalystpage.com.db.DbConnection
import catalystpage.com.db.EnvConfig
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
    val host = System.getenv("HOST") ?: "0.0.0.0"

    println("🚀 Starting Catalyst backend on $host:$port")
    println("✅ PORT environment variable: $port")

    embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    println("📦 APPLICATION MODULE LOADING - Cloud Run Ready")

    // Initialize database connection ONLY if environment variables are set
    val databaseConnection = CompletableDeferred<Boolean>()
    var databaseConfigured = false

    launch {
        try {
            // Check if database environment variables exist FIRST
            val dbHost = System.getenv("DB_HOST")
            val dbUser = System.getenv("DB_USER")
            val dbPass = System.getenv("DB_PASSWORD") ?: System.getenv("DB_PASS")

            if (dbHost != null && dbUser != null && dbPass != null) {
                println("🔗 Attempting database connection...")
                println("   Host: $dbHost")
                println("   Port: ${System.getenv("DB_PORT") ?: "3306"}")
                println("   Database: ${System.getenv("DB_NAME") ?: "catalystdb"}")
                println("   User: $dbUser")

                DbConnection.connect()
                databaseConnection.complete(true)
                databaseConfigured = true
                println("✅ Database connected successfully")
            } else {
                println("⚠️  No database configuration found - running without database")
                println("   DB_HOST: ${if (dbHost != null) "set" else "not set"}")
                println("   DB_USER: ${if (dbUser != null) "set" else "not set"}")
                println("   DB_PASSWORD: ${if (dbPass != null) "set" else "not set"}")
                databaseConnection.complete(false)
            }
        } catch (e: Exception) {
            println("❌ Database connection failed: ${e.message}")
            databaseConnection.complete(false)
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
            call.respond(HealthResponse(
                status = "running",
                timestamp = System.currentTimeMillis(),
                database = if (DbConnection.isConnected()) "connected" else "offline"
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

    println("✅ Application module loaded successfully")
    println("🌐 Server is ready to accept requests on port ${System.getenv("PORT") ?: 8080}")
}