package catalystpage.com.service

import catalystpage.com.entity.ProductEntity
import catalystpage.com.entity.ProductVariantEntity
import catalystpage.com.model.ProductVariants
import dto.ProductDTO
import dto.ProductVariantDTO
import org.jetbrains.exposed.sql.transactions.transaction

object ProductVariantService {
    fun getAll(): List<ProductVariantDTO> = transaction {
        ProductVariantEntity.all().map { it.toDTO() }
    }

    fun getById(id: Int): ProductVariantDTO? = transaction {
        ProductVariantEntity.findById(id)?.toDTO()
    }

    fun addProductVariant(data: ProductVariantDTO): ProductVariantEntity = transaction {
        val product = ProductEntity.findById(data.productId)
            ?: error("Product with ID ${data.productId} not found")

        ProductVariantEntity.new {
            this.product = product
            this.quantity = data.quantity
            this.price = data.price.toBigDecimal()
            this.stock = data.stock
            this.size = data.size
        }
    }

    fun updateProductVariant(id: Int, data: ProductVariantDTO): Boolean = transaction {
        val variant = ProductVariantEntity.findById(id) ?: return@transaction false
        val product = ProductEntity.findById(data.productId)
            ?: error("Product with ID ${data.productId} not found")

        variant.apply {
            this.product = product
            this.quantity = data.quantity
            this.price = data.price.toBigDecimal()
            this.stock = data.stock
            this.size = data.size
        }
        true
    }

    fun deleteProductVariant(id: Int): Boolean = transaction {
        ProductVariantEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    fun getByProductId(productId: Int): List<ProductVariantDTO> = transaction {
        ProductVariantEntity.find { ProductVariants.productId eq productId }
            .map { it.toDTO() }
    }

    fun searchByQuantity(keyword: String): List<ProductVariantDTO> = transaction {
        val quantityInt = keyword.toIntOrNull() ?: return@transaction emptyList()
        ProductVariantEntity.find { ProductVariants.quantity eq quantityInt }
            .map { it.toDTO() }
    }

    fun getLowStock(threshold: Int): List<ProductVariantDTO> = transaction {
        ProductVariantEntity.find { ProductVariants.stock lessEq threshold }
            .map { it.toDTO() }
    }
    fun getProductByVariantId(variantId: Int): ProductDTO? = transaction {
        ProductVariantEntity.findById(variantId)?.product?.toDTO()
    }
}