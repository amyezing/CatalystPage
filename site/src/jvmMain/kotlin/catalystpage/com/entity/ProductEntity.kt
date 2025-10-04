package catalystpage.com.entity

import catalystpage.com.model.ProductLabels
import catalystpage.com.model.ProductVariants
import catalystpage.com.model.Products
import dto.ProductDTO
import dto.ProductVariantDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ProductEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductEntity>(Products)

    var name by Products.name
    var description by Products.description
    var imageUrl by Products.imageUrl
    var createdAt by Products.createdAt
    var type by Products.type
    var price by Products.price
    val variants by ProductVariantEntity referrersOn ProductVariants.productId
    var isAvailable by Products.isAvailable

    var labels by LabelEntity via ProductLabels

    fun toDTO(includeVariants: Boolean = true, includeLabels: Boolean = true): ProductDTO = ProductDTO(
        id = this.id.value,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt?.toString(),
        type = this.type,
        price = this.price?.toDouble(),
        variants = if (includeVariants) this.variants.map { it.toDTO() } else emptyList(),
        labels = if (includeLabels) this.labels.map { it.toDTO() } else emptyList(),
        isAvailable = this.isAvailable

    )
}

class ProductVariantEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductVariantEntity>(ProductVariants)

    var product by ProductEntity referencedOn ProductVariants.productId
    var quantity by ProductVariants.quantity
    var price by ProductVariants.price
    var stock by ProductVariants.stock
    var size by ProductVariants.size


    fun toDTO(): ProductVariantDTO = ProductVariantDTO(
        id = this.id.value,
        productId = this.product.id.value,
        quantity = this.quantity,
        price = this.price.toDouble(),
        stock = this.stock,
        size = this.size,
        packSize = "${this.quantity}-bottles",
        isAvailable = this.product.isAvailable && this.stock > 0

    )
}