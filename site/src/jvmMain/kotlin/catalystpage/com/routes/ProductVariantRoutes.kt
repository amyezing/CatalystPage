package catalystpage.com.routes

import catalystpage.com.service.ProductVariantService
import dto.ProductVariantDTO
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productVariantRoutes() {
    route("/product-variants") {
        get {
            call.respond(ProductVariantService.getAll())
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if(id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }

            val variant = ProductVariantService.getById(id)
            if (variant == null) {
                call.respond(HttpStatusCode.NotFound, "Variant not found")

            } else {
                call.respond(variant)
            }
        }

        //POST /product-variants
        post {
            val data = call.receive<ProductVariantDTO>()
            val created = ProductVariantService.addProductVariant(data)
            call.respond(HttpStatusCode.Created, created.toDTO())
        }

        //PUT /product-variant/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            val data = call.receive<ProductVariantDTO>()
            val updated = ProductVariantService.updateProductVariant(id, data)

            if (updated) {
                call.respond(HttpStatusCode.OK, "Updated")
            } else {
                call.respond(HttpStatusCode.NotFound, "Variant not found")
            }
        }

        //DELETE /product-variants/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }
            val deleted = ProductVariantService.deleteProductVariant(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Variant not Found")
            }
        }


        get("/product-from-variant/{variantId}") {
            val variantId = call.parameters["variantId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid variantId")

            val product = ProductVariantService.getProductByVariantId(variantId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Product not found")

            call.respond(product)
        }
    }
}