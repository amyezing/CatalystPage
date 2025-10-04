package catalystpage.com.database

import dto.UserDTO
import dto.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.json.put
import org.jetbrains.compose.web.attributes.AutoComplete.Companion.url
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

object Constants {
    const val HOST = "catalyst-backend-184459898636.asia-southeast1.run.app"
    const val PORT = 443
}

class JsUserFetcher {
    companion object {
        private val json = Json { ignoreUnknownKeys = true; explicitNulls = true }
    }

    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private fun getUidFromLocalStorage(): String =
        window.localStorage.getItem("firebaseUserUID")
            ?: throw Exception("User not logged in")

    private fun HttpRequestBuilder.addAuthHeader(uid: String) {
        headers.append("X-Firebase-Uid", uid)
    }

    suspend fun fetchCurrentUser(): UserDTO {
        val uid = getUidFromLocalStorage()
        return client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
                port = Constants.PORT
                encodedPath = "/api/users/me"
            }
            addAuthHeader(uid)
        }.body()
    }

    suspend fun updateUser(name: String? = null, phone: String? = null) {
        val uid = getUidFromLocalStorage()
        val payload = buildJsonObject {
            put("firebase_uid", JsonPrimitive(uid))
            name?.takeIf { it.isNotBlank() }?.let { put("name", JsonPrimitive(it)) }
            if (phone == null) put("phone", JsonNull)
            else if (phone.isNotBlank()) {
                if (!phone.matches(Regex("^\\d{11}\$"))) throw Exception("Invalid phone format")
                put("phone", JsonPrimitive(phone))
            }
        }

        client.put {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
                port = Constants.PORT
                encodedPath = "/api/users/me"
            }
            contentType(ContentType.Application.Json)
            addAuthHeader(uid)
            setBody(payload)
        }
    }

    suspend fun syncUserWithBackend(uid: String, email: String) {
        val payload = json.encodeToString(UserDTO(firebaseUid = uid, email = email))
        client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
                port = Constants.PORT
                encodedPath = "/api/auth/sync"
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun deleteCurrentUser(): Boolean {
        val uid = getUidFromLocalStorage()
        val response = client.delete {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
                port = Constants.PORT
                encodedPath = "/api/users/me"
            }
            addAuthHeader(uid)
        }
        return response.status.isSuccess()
    }
}