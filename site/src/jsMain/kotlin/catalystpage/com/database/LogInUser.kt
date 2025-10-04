package catalystpage.com.database

import androidx.compose.runtime.MutableState
import catalystpage.com.firebase.*
import dto.Role
import dto.UserDTO
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit

val auth = getAuth(FirebaseConfig.firebaseApp)

val jsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun signInWithGoogle(onUserSynced: (UserDTO) -> Unit) {
    val provider = GoogleAuthProvider()
    signInWithPopup(auth, provider)
        .then { result ->
            val user = result.user
            if (user != null) {
                MainScope().launch {
                    try {
                        val adminEmailsResp = window.fetch("http://localhost:8081/api/admin-emails").await()
                        val adminEmailsDynamic = adminEmailsResp.json().await().asDynamic().emails as Array<dynamic>
                        val adminEmails = adminEmailsDynamic.mapNotNull { it as? String }

                        val userEmail = user.email as? String
                        val userRole = if (userEmail != null && adminEmails.any { it.lowercase() == userEmail.lowercase() }) {
                            Role.ADMIN
                        } else null

                        val userData = UserDTO(
                            firebaseUid = user.uid ?: "",
                            email = userEmail,
                            name = user.displayName as? String,
                            phone = user.phoneNumber as? String,
                            role = userRole
                        )

                        val jsonBody = Json.encodeToString(userData)
                        window.localStorage.setItem("firebaseUserUID", user.uid)
                        window.localStorage.setItem("firebaseUserEmail", userEmail ?: "")

                        window.fetch(
                            "http://localhost:8081/api/auth/sync",
                            RequestInit(
                                method = "POST",
                                headers = js("{ 'Content-Type': 'application/json' }"),
                                body = jsonBody
                            )
                        ).then { resp ->
                            resp.text().then { rawText ->
                                val syncedUser = jsonParser.decodeFromString<UserDTO>(rawText)
                                onUserSynced(syncedUser)
                            }
                        }.catch { e -> console.error("❌ Failed to sync user:", e) }

                    } catch (e: dynamic) {
                        console.error("❌ Error fetching admin emails:", e)
                    }
                }
            }
        }
        .catch { error ->
            console.error("❌ Sign-in failed:", error)
        }
}



fun getLoggedInUser(): String? {
    val user = auth.currentUser

    return if (user != null) {
        console.log("User is already logged in: ${user.email}")
        user.email as? String
    } else {
        console.log("No user logged in")
        null
    }
}


fun logOutUser() {
    signOut(auth)
        .then {
            window.localStorage.removeItem("firebaseUserUID")
            window.localStorage.removeItem("firebaseUserEmail")
            console.log("User logged out successfully")
        }
        .catch { error ->
            console.error("Logout failed:", error)
        }
}

