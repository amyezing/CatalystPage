package catalystpage.com.admin.fetcher

import admin.dto.AdminPaymentSummaryDTO
import admin.dto.PaymentStatusUpdateRequest
import admin.dto.PaymentStatusUpdateResponse
import catalystpage.com.util.Constants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val paymentApiClient = HttpClient {
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

suspend fun fetchAdminPaymentSummary(): List<AdminPaymentSummaryDTO> {
    return try {
        paymentApiClient.get("/api/payments/admin/summary").body()
    } catch (e: Exception) {
        console.error("❌ Failed to fetch admin payment summary", e)
        emptyList()
    }
}

suspend fun updatePaymentStatus(orderId: Int, newStatus: String): Boolean {
    return try {
        val response: PaymentStatusUpdateResponse = paymentApiClient.put("/api/payments/admin/status") {
            contentType(ContentType.Application.Json)
            setBody(
                PaymentStatusUpdateRequest(orderId, newStatus)
            )
        }.body()

        response.success
    } catch (e: Exception) {
        console.error("❌ Error updating payment status", e)
        false
    }
}