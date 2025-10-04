package catalystpage.com.database

import catalystpage.com.util.Constants
import dto.PaymentDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.xhr.FormData

object JsPaymentFetcher {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            url {
                protocol = if (Constants.PORT == 443) URLProtocol.HTTPS else URLProtocol.HTTP
                host = Constants.HOST
                port = Constants.PORT
            }
        }
    }


    suspend fun getPaymentByOrderId(orderId: Int): PaymentDTO? {
        return try {
            client.get("/api/payments/order/$orderId").body()
        } catch (e: Exception) {
            println("🚨 Failed to fetch payment for order $orderId: ${e.message}")
            null
        }
    }

    suspend fun updatePaymentStatus(paymentId: Int, status: String): Boolean {
        return try {
            val response = client.put("/api/payments/$paymentId/status") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("status" to status))
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("❌ Failed to update payment status: ${e.message}")
            false
        }
    }

    suspend fun uploadPaymentProof(orderId: Int, file: File, onComplete: (String) -> Unit) {
        try {
            val formData = FormData().apply {
                append("orderId", orderId.toString())
                append("amount", "0") // Or actual amount
                append("method", "Gcash")
                append("reference", "")
                append("proof", file, file.name)
            }

            val requestInit = RequestInit(
                method = "POST",
                body = formData
            )

            val backendUrl =  "https://${Constants.HOST}"
            val response = window.fetch("$backendUrl/api/payments/submit", requestInit).await()

            if (response.asDynamic().ok as Boolean) {
                onComplete("✅ Payment uploaded successfully.")
            } else {
                val errorText = response.text().await()
                onComplete("❌ Upload failed: $errorText")
            }
        } catch (e: Throwable) {
            onComplete("🚨 Error: ${e.message}")
        }
    }

    suspend fun getPaymentStatus(orderId: Int): String {
        return try {
            val response = client.get("/api/payments/status") {
                parameter("orderId", orderId)
            }.body<Map<String, String>>() // 🔁 expects proper JSON

            response["status"] ?: "NONE"
        } catch (e: Exception) {
            console.error("Error checking payment status: $e")
            "ERROR"
        }
    }
}