package catalystpage.com.database

import catalystpage.com.util.Constants
import dto.LabelDTO
import dto.ProductDTO
import dto.ProductVariantDTO
import fetcher.ProductFetcher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.files.File
import org.w3c.xhr.FormData
import org.w3c.fetch.RequestInit



class JsProductFetcher : ProductFetcher {

    private val client = HttpClient(Js) {
        expectSuccess = false // Don't throw automatically on 4xx/5xx
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = Constants.HOST
            }
        }
    }

    // -------------------
    // Generic handler
    // -------------------
    private suspend inline fun <reified T> handleResponse(response: HttpResponse): T? {
        return when (response.status.value) {
            in 200..299 -> response.body()
            503 -> {
                console.log("Backend database unavailable - returning empty data")
                null
            }
            else -> {
                console.log("HTTP ${response.status.value} error: ${response.status.description}")
                null
            }
        }
    }

    // -------------------
    // Fetch operations
    // -------------------
    override suspend fun fetchProducts(): List<ProductDTO> {
        return try {
            val response = client.get("api/products")
            handleResponse(response) ?: emptyList()
        } catch (e: Exception) {
            console.log("Network error fetching products: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchProductVariants(productId: String): List<ProductVariantDTO> {
        return try {
            val response = client.get("api/products/$productId/variants")
            handleResponse(response) ?: emptyList()
        } catch (e: Exception) {
            console.log("Network error fetching variants: ${e.message}")
            emptyList()
        }
    }

    // -------------------
    // Create / Update
    // -------------------
    suspend fun createProduct(product: ProductDTO, imageFile: File?): ProductDTO {
        return try {
            val formData = FormData().apply {
                append("name", product.name)
                append("description", product.description ?: "")
                append("price", product.price.toString())
                append("variants", Json.encodeToString(product.variants))
                append("labels", Json.encodeToString(product.labels.map { it.id }))
                if (imageFile != null) append("image", imageFile, imageFile.name)
            }

            val response = client.post("api/products") {
                setBody(formData)
            }

            handleResponse<ProductDTO>(response)
                ?: throw Exception("Failed to create product: ${response.status.description}")
        } catch (e: Exception) {
            console.log("Network error creating product with image: ${e.message}")
            throw e
        }
    }

    suspend fun updateProduct(product: ProductDTO): ProductDTO {
        return try {
            val response = client.put("api/products/${product.id}") {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            handleResponse<ProductDTO>(response)
                ?: throw Exception("Failed to update product: ${response.status.description}")
        } catch (e: Exception) {
            console.log("Network error updating product: ${e.message}")
            throw e
        }
    }

    suspend fun fetchLabels(): List<LabelDTO> {
        return try {
            val response = client.get("api/labels")
            handleResponse(response) ?: emptyList()
        } catch (e: Exception) {
            console.log("Network error fetching labels: ${e.message}")
            emptyList()
        }
    }
}


