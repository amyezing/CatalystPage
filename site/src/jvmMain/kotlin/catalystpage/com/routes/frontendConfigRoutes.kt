package catalystpage.com.routes

import catalystpage.com.db.EnvConfig
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.frontendConfigRoutes() {
    get("frontend-config") {
        call.respond(
            mapOf(
                "apiKey" to EnvConfig.firebaseApiKey,
                "authDomain" to EnvConfig.firebaseAuthDomain,
                "projectId" to EnvConfig.firebaseProjectId,
                "storageBucket" to EnvConfig.firebaseStorageBucket,
                "messagingSenderId" to EnvConfig.firebaseMessagingSenderId,
                "appId" to EnvConfig.firebaseAppId
            )
        )
    }
}