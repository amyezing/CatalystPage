package catalystpage.com.model

import org.jetbrains.exposed.dao.id.IntIdTable

object CartItems : IntIdTable("cart_items") {
    val firebaseUid = varchar("firebase_uid", 128)
    val quantity = integer("quantity").default(1)
    val productVariantId = integer("product_variant_id").nullable()
}