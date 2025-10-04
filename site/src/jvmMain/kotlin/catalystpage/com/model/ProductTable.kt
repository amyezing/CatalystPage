package catalystpage.com.model

import dto.ProductType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp


object Products : IntIdTable("products") {
    val name: Column<String> = varchar("name", 255)
    val description: Column<String?> = text("description").nullable()
    val imageUrl: Column<String?> = text("image_url").nullable()
    val createdAt: Column<java.time.Instant?> = timestamp("created_at").nullable()
    val type: Column<ProductType> = enumerationByName("type", 10, ProductType::class)
    val price: Column<java.math.BigDecimal?> = decimal("price", 10, 2).nullable()
    val isAvailable = bool("is_available").default(true)
}

object ProductVariants : IntIdTable("product_variants") {
    val productId = reference("product_id", Products)
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)
    val stock = integer("stock")
    val size = varchar("size", 50).nullable()
}