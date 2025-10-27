package catalystpage.com.routes

import catalystpage.com.db.DbConnection
import catalystpage.com.service.ProductService
import catalystpage.com.service.ProductVariantService
import catalystpage.com.util.GcsService
import dto.LabelDTO
import dto.ProductDTO
import dto.ProductVariantDTO
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json

fun Route.productRoutes() {
    route("/products") {
        get {
            // ✅ Add database check
            if (!DbConnection.isConnected()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Database unavailable",
                    "message" to "Please try again later"
                ))
                return@get
            }

            try {
                val products = ProductService.getAll()
                call.respond(products)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch products: ${e.message}"))
            }
        }

        get("{id}") {
            // ✅ Add database check
            if (!DbConnection.isConnected()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Database unavailable",
                    "message" to "Please try again later"
                ))
                return@get
            }

            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            try {
                val product = ProductService.getById(id)
                if (product == null) {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                } else {
                    call.respond(product)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch product: ${e.message}"))
            }
        }

        get("{id}/variants") {
            // ✅ Add database check
            if (!DbConnection.isConnected()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Database unavailable",
                    "message" to "Please try again later"
                ))
                return@get
            }

            val productId = call.parameters["id"]?.toIntOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@get
            }

            try {
                val variants = ProductVariantService.getByProductId(productId)
                call.respond(variants)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch variants: ${e.message}"))
            }
        }

        post {
            // ✅ Add database check (after parsing multipart but before service calls)
            val multipart = call.receiveMultipart()
            var name: String? = null
            var description: String? = null
            var price: Double? = null
            var imageUrl: String? = null
            var variantsJson: String? = null
            var labelsJson: String? = null
            var isAvailable: Boolean = true

            val json = Json { ignoreUnknownKeys = true }

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "description" -> description = part.value
                            "price" -> price = part.value.toDoubleOrNull()
                            "variants" -> variantsJson = part.value
                            "labels" -> labelsJson = part.value
                            "isAvailable" -> isAvailable = part.value.toBooleanStrictOrNull() ?: true
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toByteArray()
                        val originalFileName = part.originalFileName ?: "unknown.png"
                        imageUrl = GcsService.uploadFile(originalFileName, bytes)
                    }
                    else -> {}
                }
                part.dispose()
            }

            // ✅ Check database AFTER parsing multipart data
            if (!DbConnection.isConnected()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Database unavailable",
                    "message" to "Please try again later"
                ))
                return@post
            }

            if (name == null || price == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing name or price")
                return@post
            }

            try {
                val variants = variantsJson?.let { json.decodeFromString<List<ProductVariantDTO>>(it) } ?: emptyList()
                val labelIds = labelsJson?.let { json.decodeFromString<List<Int>>(it) } ?: emptyList()

                val created = ProductService.addProduct(
                    ProductDTO(
                        id = 0,
                        name = name!!,
                        description = description,
                        price = price!!,
                        imageUrl = imageUrl,
                        variants = variants,
                        labels = labelIds.map { LabelDTO(it, "", null, 0) },
                        isAvailable = isAvailable
                    )
                )

                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create product: ${e.message}"))
            }
        }

        put("{id}") {
            // ✅ Add database check
            if (!DbConnection.isConnected()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Database unavailable",
                    "message" to "Please try again later"
                ))
                return@put
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            try {
                val updatedProduct = call.receive<ProductDTO>()
                val success = ProductService.updateProduct(id, updatedProduct)

                if (!success) {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                } else {
                    val product = ProductService.getById(id)
                    call.respond(HttpStatusCode.OK, product!!)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update product: ${e.message}"))
            }
        }

        delete("{id}") {
            // ✅ Add database check
            if (!DbConnection.isConnected()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Database unavailable",
                    "message" to "Please try again later"
                ))
                return@delete
            }

            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            try {
                val deleted = ProductService.deleteProduct(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete product: ${e.message}"))
            }
        }
    }
}