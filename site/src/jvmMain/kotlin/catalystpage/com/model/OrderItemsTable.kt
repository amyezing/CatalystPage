package catalystpage.com.model

import org.jetbrains.exposed.dao.id.IntIdTable

object OrderItems : IntIdTable("order_items") {
    val order = reference("order_id", Orders)
    val productVariant = reference("product_variant_id", ProductVariants).nullable()
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)
}