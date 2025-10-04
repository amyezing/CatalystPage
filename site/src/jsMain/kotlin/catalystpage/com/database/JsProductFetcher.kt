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
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }

        defaultRequest {
            url {
                protocol = if (Constants.PORT == 443) URLProtocol.HTTPS else URLProtocol.HTTP
                host = Constants.HOST
                port = Constants.PORT
            }
        }
    }

    override suspend fun fetchProducts(): List<ProductDTO> =
        client.get("api/products").body()

    suspend fun fetchProductVariants(productId: String): List<ProductVariantDTO> =
        client.get("api/products/$productId/variants").body()

    suspend fun addProduct(product: ProductDTO, imageFile: File?): ProductDTO {
        val formData = FormData().apply {
            append("name", product.name)
            append("description", product.description ?: "")
            append("price", product.price.toString())
            append("variants", Json.encodeToString(product.variants))
            append("labels", Json.encodeToString(product.labels.map { it.id }))
            if (imageFile != null) append("image", imageFile, imageFile.name)
        }

        return client.post("api/products") {
            setBody(formData) // <-- use setBody instead of body = formData
        }.body()
    }

    suspend fun fetchLabels(): List<LabelDTO> =
        client.get("api/labels").body()

    suspend fun addLabelToProduct(labelId: Int, productId: Int) =
        client.post("api/labels/$labelId/products/$productId")

    suspend fun removeLabelFromProduct(labelId: Int, productId: Int) =
        client.delete("api/labels/$labelId/products/$productId")

    suspend fun getProductsByLabel(labelId: Int): List<ProductDTO> =
        client.get("api/labels/$labelId/products").body()

    suspend fun updateProduct(product: ProductDTO): ProductDTO =
        client.put("api/products/${product.id}") {
            contentType(ContentType.Application.Json)
            setBody(product)
        }.body()
}


