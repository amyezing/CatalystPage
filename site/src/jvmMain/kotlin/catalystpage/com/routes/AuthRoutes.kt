package catalystpage.com.routes

import catalystpage.com.db.EnvConfig
import catalystpage.com.service.UserService
import dto.Role
import dto.UserDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {

    route("/auth") {
        post("/sync") {
            val userDTO = try {
                call.receive<UserDTO>().also {
                    println("✅ Received userDTO: $it")
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request body")
                )
                return@post
            }

            val firebaseUid = userDTO.firebaseUid.trim()

            if (firebaseUid.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing or empty firebaseUid")
                )
                return@post
            }

            println("Auth sync called with UID: $firebaseUid")

            try {
                val existingUser = UserService.getByFirebaseUid(firebaseUid)

                val savedUserDTO = if (existingUser == null) {
                    println("Creating new user for UID: $firebaseUid")

                    // Use EnvConfig.adminEmails instead of hardcoded list
                    val assignedRole = if (userDTO.email?.lowercase() in EnvConfig.adminEmails.map { it.lowercase() }) {
                        Role.ADMIN
                    } else {
                        Role.USER
                    }

                    val finalUserDTO = userDTO.copy(role = assignedRole)

                    UserService.createUser(finalUserDTO).toDTO()
                } else {
                    println("Checking for update for UID: $firebaseUid")

                    // Preserve role from DB; do not allow frontend to override role
                    val updatedDTO = userDTO.copy(
                        role = existingUser.role,
                        name = existingUser.name // keep DB name, ignore Firebase name
                    )

                    val changesNeeded = (updatedDTO.email != existingUser.email) ||
                            (updatedDTO.name != existingUser.name) ||
                            (updatedDTO.phone != existingUser.phone)

                    if (!changesNeeded) {
                        println("No changes detected for UID: $firebaseUid — skipping update")
                        existingUser.toDTO()
                    } else {
                        println("Updating user for UID: $firebaseUid")
                        UserService.updateUserRole(firebaseUid, updatedDTO)

                        val updatedUser = UserService.getByFirebaseUid(firebaseUid)
                        if (updatedUser == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "User disappeared after update")
                            )
                            return@post
                        }

                        updatedUser.toDTO()
                    }
                }

                call.respond(HttpStatusCode.OK, savedUserDTO)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error syncing user: ${e.message}")
                )
            }
        }
    }
}