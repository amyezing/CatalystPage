package catalystpage.com.database

import dto.LabelDTO
import dto.ProductDTO
import dto.ProductVariantDTO
import fetcher.ProductFetcher
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.files.File
import org.w3c.xhr.FormData
import org.w3c.fetch.RequestInit



class JsProductFetcher : ProductFetcher {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val BASE_URL = "http://localhost:8081/api"
    }

    override suspend fun fetchProducts(): List<ProductDTO> {
        val response = window.fetch("$BASE_URL/products").await()
        if (!response.ok) {
            val errorBody = response.text().await()
            console.error("❌ Failed to fetch products:", response.status, response.statusText, errorBody)
            throw Exception("Failed to fetch products: ${response.status} ${response.statusText}")
        }
        val text = response.text().await()
        return if (text.isBlank()) emptyList() else json.decodeFromString(text)
    }

    suspend fun fetchProductVariants(productId: String): List<ProductVariantDTO> {
        val response = window.fetch("$BASE_URL/products/$productId/variants").await()
        if (!response.ok) {
            val errorBody = response.text().await()
            console.error("❌ Failed to fetch product variants:", response.status, response.statusText, errorBody)
            throw Exception("Failed to fetch product variants: ${response.status} ${response.statusText}")
        }
        val text = response.text().await()
        return if (text.isBlank()) emptyList() else json.decodeFromString(text)
    }

    suspend fun addProduct(product: ProductDTO, imageFile: File?): ProductDTO {
        val formData = FormData().apply {
            append("name", product.name)
            append("description", product.description ?: "")
            append("price", product.price.toString())

            val variantsJson = json.encodeToString(product.variants)
            append("variants", variantsJson)

            val labelsJson = json.encodeToString(product.labels.map { it.id }) // send only IDs
            append("labels", labelsJson)

            if (imageFile != null) {
                append("image", imageFile, imageFile.name)
            }
        }

        val requestInit: RequestInit = js("{}").unsafeCast<RequestInit>().apply {
            method = "POST"
            body = formData
        }

        val response = window.fetch("$BASE_URL/products", requestInit).await()
        if (!response.ok) {
            val errorBody = response.text().await()
            throw Exception("❌ Failed to add product: ${response.status} $errorBody")
        }
        val text = response.text().await()
        return json.decodeFromString(text)
    }

    suspend fun fetchLabels(): List<LabelDTO> {
        val response = window.fetch("$BASE_URL/labels").await()
        if (!response.ok) throw Exception("Failed to fetch labels")
        val text = response.text().await()
        return if (text.isBlank()) emptyList() else json.decodeFromString(text)
    }

    // --- updated according to new routes ---
    suspend fun addLabelToProduct(labelId: Int, productId: Int) {
        val requestInit: RequestInit = js("{}").unsafeCast<RequestInit>().apply {
            method = "POST"
        }
        val response = window.fetch("$BASE_URL/labels/$labelId/products/$productId", requestInit).await()
        if (!response.ok) throw Exception("Failed to add label: ${response.status}")
    }

    suspend fun removeLabelFromProduct(labelId: Int, productId: Int) {
        val requestInit: RequestInit = js("{}").unsafeCast<RequestInit>().apply {
            method = "DELETE"
        }
        val response = window.fetch("$BASE_URL/labels/$labelId/products/$productId", requestInit).await()
        if (!response.ok) throw Exception("Failed to remove label: ${response.status}")
    }

    suspend fun getProductsByLabel(labelId: Int): List<ProductDTO> {
        val response = window.fetch("$BASE_URL/labels/$labelId/products").await()
        if (!response.ok) throw Exception("Failed to fetch products by label")
        val text = response.text().await()
        return if (text.isBlank()) emptyList() else json.decodeFromString(text)
    }

    suspend fun updateProduct(product: ProductDTO): ProductDTO {
        val requestInit: RequestInit = js("{}").unsafeCast<RequestInit>().apply {
            method = "PUT" // or PATCH if your backend uses that
            headers = js("{}")
            headers["Content-Type"] = "application/json"
            body = json.encodeToString(product)
        }

        val response = window.fetch("$BASE_URL/products/${product.id}", requestInit).await()
        if (!response.ok) {
            val errorBody = response.text().await()
            throw Exception("❌ Failed to update product: ${response.status} $errorBody")
        }

        val text = response.text().await()
        return json.decodeFromString(text)
    }

}



