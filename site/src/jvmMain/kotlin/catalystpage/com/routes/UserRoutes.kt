package catalystpage.com.routes

import catalystpage.com.service.UserService
import dto.UpdateUserDTO
import dto.UserDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.userRoutes() {
    route("/users") {

        // Get all users (usually for admin use)
        get {
            val users = UserService.getAll().map { it.toDTO() }
            call.respond(users)
        }

        get("/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val user = UserService.getUserWithEcoPoints(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        // Get user by Firebase UID
        get("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing Firebase UID")
                return@get
            }

            val user = UserService.getByFirebaseUid(firebaseUid)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user.toDTO())
            }
        }

        // Update user info by Firebase UID
        put("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing Firebase UID")
                return@put
            }

            val updateData = call.receive<UserDTO>()
            val currentUser = UserService.getByFirebaseUid(firebaseUid)

            if (currentUser == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@put
            }

            // Preserve role if none is provided in the update
            val finalRole = updateData.role ?: currentUser.role

            val changesNeeded =
                (updateData.email != null && updateData.email != currentUser.email) ||
                        (updateData.name != null && updateData.name != currentUser.name) ||
                        (updateData.phone != null && updateData.phone != currentUser.phone) ||
                        (finalRole != currentUser.role)

            if (!changesNeeded) {
                call.respond(HttpStatusCode.OK, "No changes detected, update skipped.")
                return@put
            }

            val updatedDTO = updateData.copy(role = finalRole)

            val updated = UserService.updateUserRole(firebaseUid, updatedDTO)

            if (updated) {
                val updatedUser = UserService.getByFirebaseUid(firebaseUid)
                call.respond(updatedUser?.toDTO() ?: HttpStatusCode.InternalServerError)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update user")
            }
        }

        // Delete user by Firebase UID
        delete("/{firebaseUid}") {
            val firebaseUid = call.parameters["firebaseUid"]
            if (firebaseUid == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing Firebase UID")
                return@delete
            }

            val deleted = UserService.deleteUser(firebaseUid)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }

        // Create a new user
        post {
            val userDTO = call.receive<UserDTO>()

            val existing = UserService.getByFirebaseUid(userDTO.firebaseUid)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
                return@post
            }

            val createdUser = UserService.createUser(userDTO)
            call.respond(createdUser.toDTO())
        }

        post("/findOrCreate") {
            val userDTO = try {
                call.receive<UserDTO>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                return@post
            }

            val firebaseUid = userDTO.firebaseUid
            if (firebaseUid.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "firebaseUid is required")
                return@post
            }

            val userEntity = UserService.findOrCreate(firebaseUid, userDTO.email ?: "")
            call.respond(userEntity.toDTO())
        }

        // Get the logged-in user's profile from header
        get("/me") {
            val uid = call.request.headers["X-Firebase-Uid"]
            if (uid.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, "Missing UID header")
                return@get
            }

            val user = UserService.getByFirebaseUid(uid)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            call.respond(user.toDTO())
        }


        put("/me") {
            val uid = call.request.headers["X-Firebase-Uid"]
            if (uid.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, "Missing UID header")
                return@put
            }

            val updateRequest = try {
                call.receive<UpdateUserDTO>().copy(firebaseUid = uid)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid body")
                return@put
            }

            val user = UserService.getByFirebaseUid(uid)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@put
            }

            val updated = UserService.updateUsers(updateRequest)
            if (updated) {
                val updatedUser = UserService.getByFirebaseUid(uid)
                call.respond(updatedUser?.toDTO() ?: HttpStatusCode.InternalServerError)
            } else {
                call.respond(HttpStatusCode.OK, "No changes detected")
            }
        }

        delete("/me") {
            val uid = call.request.headers["X-Firebase-Uid"]
            if (uid.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, "Missing UID header")
                return@delete
            }

            val deleted = UserService.deleteUser(uid)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}