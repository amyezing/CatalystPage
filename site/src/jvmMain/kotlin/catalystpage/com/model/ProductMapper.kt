package catalystpage.com.model

import catalystpage.com.entity.ProductEntity
import catalystpage.com.entity.ProductVariantEntity
import dto.ProductDTO
import dto.ProductVariantDTO

fun ProductDTO.toEntity(): ProductEntity = ProductEntity.new {
    name = this@toEntity.name
    description = this@toEntity.description
    imageUrl = this@toEntity.imageUrl
    createdAt = this@toEntity.createdAt?.let { java.time.Instant.parse(it) }
    type = this@toEntity.type
    price = this@toEntity.price?.toBigDecimal()

}


fun ProductVariantDTO.toEntity(): ProductVariantEntity = ProductVariantEntity.new {
    product = ProductEntity.findById(this@toEntity.productId) ?: error("Product not found")
    quantity = this@toEntity.quantity
    price = this@toEntity.price.toBigDecimal()
    stock = this@toEntity.stock
    size = this@toEntity.size

}

