package catalystpage.com.routes

import catalystpage.com.service.ProductService
import catalystpage.com.service.ProductVariantService
import catalystpage.com.util.GcsService
import dto.LabelDTO
import dto.ProductDTO
import dto.ProductVariantDTO
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import java.io.File

fun Route.productRoutes() {

    route("/products") {
        get {
            val products = ProductService.getAll()
            call.respond(products)
        }

        // GET /products/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val product = ProductService.getById(id)
            if (product == null) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } else {
                call.respond(product)
            }
        }

        get("{id}/variants") {
            val productId = call.parameters["id"]?.toIntOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@get
            }

            val variants = ProductVariantService.getByProductId(productId)
            call.respond(variants)
        }

        // POST /products//change to cloud bucket
        post {
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
                            "labels" -> labelsJson = part.value // 🔹 capture labels
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

            if (name == null || price == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing name or price")
                return@post
            }

            val variants = variantsJson?.let { json.decodeFromString<List<ProductVariantDTO>>(it) } ?: emptyList()
            val labelIds = labelsJson?.let { json.decodeFromString<List<Int>>(it) } ?: emptyList() // 🔹 decode IDs

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
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val updatedProduct = call.receive<ProductDTO>()
            val success = ProductService.updateProduct(id, updatedProduct)

            if (!success) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } else {
                // Respond with the updated product
                val product = ProductService.getById(id)
                call.respond(HttpStatusCode.OK, product!!)
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            val deleted = ProductService.deleteProduct(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            }
        }
    }
}