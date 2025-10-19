package catalystpage.com.database

import androidx.compose.runtime.MutableState
import catalystpage.com.firebase.*
import catalystpage.com.util.Constants
import dto.Role
import dto.UserDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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

object AuthFetcher {
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    suspend fun syncUserWithBackend(user: UserDTO): UserDTO {
        return client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
                port = Constants.PORT
                encodedPath = "/api/auth/sync"
            }
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    suspend fun fetchAdminEmails(): List<String> {
        return client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
                port = Constants.PORT
                encodedPath = "/api/admin-emails"
            }
        }.body()
    }
}

fun signInWithGoogle(onUserSynced: (UserDTO) -> Unit) {
    val provider = GoogleAuthProvider()
    signInWithPopup(auth, provider)
        .then { result ->
            val user = result.user
            if (user != null) {
                MainScope().launch {
                    try {
                        val adminEmails = AuthFetcher.fetchAdminEmails()
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

                        val syncedUser = AuthFetcher.syncUserWithBackend(userData)

                        window.localStorage.setItem("firebaseUserUID", user.uid)
                        window.localStorage.setItem("firebaseUserEmail", userEmail ?: "")

                        onUserSynced(syncedUser)
                    } catch (e: dynamic) {
                        console.error("❌ Error during Google sign-in sync:", e)
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
        .catch { error -> console.error("Logout failed:", error) }
}