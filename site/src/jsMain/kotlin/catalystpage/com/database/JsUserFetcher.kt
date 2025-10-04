package catalystpage.com.database

import dto.UserDTO
import dto.UserProfile
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

class JsUserFetcher {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = true
        }
    }
    private fun getUidFromLocalStorage(): String =
        window.localStorage.getItem("firebaseUserUID")
            ?: throw Exception("User not logged in")


    suspend fun fetchCurrentUser(): UserDTO {
        val uid = window.localStorage.getItem("firebaseUserUID")

        if (uid.isNullOrBlank()) {
            console.warn("⚠️ No Firebase UID in localStorage")
            throw Exception("User not logged in")
        }

        val headers = Headers().apply {
            append("X-Firebase-Uid", uid)
        }

        val response = window.fetch(
            "http://localhost:8081/api/users/me",
            RequestInit(
                method = "GET",
                headers = headers
            )
        ).await()

        if (!response.ok) {
            val errorBody = response.text().await()
            console.error("❌ Failed to fetch user:", response.status, response.statusText, errorBody)
            throw Exception("Failed to fetch user: ${response.status} ${response.statusText}")
        }

        val responseText = response.text().await()

        if (responseText.isBlank()) {
            console.warn("⚠️ User response body is empty")
            throw Exception("User response is empty")
        }

        return json.decodeFromString(responseText)
    }

    suspend fun updateUser(name: String? = null, phone: String? = null) {
        val uid = getUidFromLocalStorage()

        val payload = buildJsonObject {
            put("firebase_uid", JsonPrimitive(uid))
            name?.takeIf { it.isNotBlank() }?.let { put("name", JsonPrimitive(it)) }

            // explicit deletion: phone == null -> put JSON null
            if (phone == null) {
                put("phone", JsonNull)
            } else if (phone.isNotBlank()) {
                if (!phone.matches(Regex("^\\d{11}\$"))) {
                    throw Exception("Invalid phone format")
                }
                put("phone", JsonPrimitive(phone))
            }
        }

        val jsonString = Json.encodeToString(payload)
        console.log("📤 Payload being sent:", jsonString)

        val response = window.fetch(
            "http://localhost:8081/api/users/me",
            RequestInit(
                method = "PUT",
                headers = Headers().apply {
                    append("Content-Type", "application/json")
                    append("X-Firebase-Uid", uid)
                },
                body = jsonString
            )
        ).await()

        if (!response.ok) {
            val err = response.text().await()
            console.error("Failed to update user:", response.status, err)
            throw Exception("Failed to update user: ${response.status}")
        }
    }



    suspend fun syncUserWithBackend(uid: String, email: String) {
        val headers = Headers().apply {
            append("Content-Type", "application/json")
        }

        val userPayload = json.encodeToString(UserDTO(firebaseUid = uid, email = email))

        val response = window.fetch(
            "http://localhost:8081/api/auth/sync",
            RequestInit(
                method = "POST",
                headers = headers,
                body = userPayload
            )
        ).await()

        if (!response.ok) {
            val error = response.text().await()
            console.error("❌ Failed to sync user with backend:", error)
            throw Exception("Failed to sync user: ${response.status} ${response.statusText}")
        } else {
            console.log("✅ User synced with backend")
        }
    }



    suspend fun deleteCurrentUser(): Boolean {
        val uid = window.localStorage.getItem("firebaseUserUID")
            ?: throw Exception("User not logged in")

        val headers = Headers().apply {
            append("X-Firebase-Uid", uid)
        }

        val response = window.fetch(
            "http://localhost:8081/api/users/me",
            RequestInit(
                method = "DELETE",
                headers = headers
            )
        ).await()

        return if (response.ok) {
            console.log("✅ User deleted successfully")
            true
        } else {
            val error = response.text().await()
            console.error("❌ Failed to delete user:", error)
            false
        }
    }

}