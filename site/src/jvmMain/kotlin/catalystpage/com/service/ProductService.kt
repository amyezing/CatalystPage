package catalystpage.com.service

import catalystpage.com.db.DbConnection
import catalystpage.com.entity.LabelEntity
import catalystpage.com.entity.ProductEntity
import catalystpage.com.entity.ProductVariantEntity
import catalystpage.com.model.*
import catalystpage.com.service.admin.AuditService
import dto.ProductDTO
import dto.ProductVariantDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object ProductService {

    fun getAll(): List<ProductDTO> {
        checkDatabaseConnection()
        return transaction {
            ProductEntity.all().map { it.toDTO() }
        }
    }

    fun getById(id: Int): ProductDTO? {
        checkDatabaseConnection()
        return transaction {
            ProductEntity.findById(id)?.toDTO(includeVariants = true, includeLabels = true)
        }
    }

    fun addProduct(data: ProductDTO, createdBy: Int? = null): ProductDTO = transaction {
        checkDatabaseConnection()
        val product = ProductEntity.new {
            name = data.name
            description = data.description
            imageUrl = data.imageUrl
            price = data.price?.toBigDecimal()
            type = data.type
            isAvailable = data.isAvailable
            createdAt = data.createdAt?.let { Instant.parse(it) } ?: Instant.now()
        }

        // --- save variants
        data.variants.forEach { v ->
            ProductVariantEntity.new {
                this.product = product
                price = v.price.toBigDecimal()
                stock = v.stock
                size = v.size
                quantity = v.quantity
            }
        }

        // --- save labels 🔹
        val newLabels = data.labels.mapNotNull { LabelEntity.findById(it.id) }
        if (newLabels.isNotEmpty()) {
            product.labels = SizedCollection(newLabels)
        }
        AuditService.log(
            adminId = createdBy,
            action = "CREATE",
            targetTable = "products",
            targetId = product.id.value
        )

        product.toDTO(includeVariants = true, includeLabels = true) // <-- make sure to return labels
    }

    fun deleteProduct(id: Int): Boolean = transaction {
        checkDatabaseConnection()
        ProductEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    fun updateProduct(id: Int, data: ProductDTO, updatedBy: Int? = null): Boolean = transaction {
        checkDatabaseConnection()
        ProductEntity.findById(id)?.let { product ->
            val oldData = product.toDTO(includeVariants = true)
            product.apply {
                name = data.name
                description = data.description
                imageUrl = data.imageUrl
                createdAt = data.createdAt?.let { Instant.parse(it) }
                type = data.type
                price = data.price?.toBigDecimal()
                isAvailable = data.isAvailable
            }

            AuditService.log(
                adminId = updatedBy,
                action = "UPDATE",
                targetTable = "products",
                targetId = product.id.value
            )
            true
        } ?: false
    }


    fun searchByName(keyword: String): List<ProductDTO> {
        checkDatabaseConnection() // ← ADD THIS
        return transaction {
            ProductEntity.find { Products.name like "%$keyword%" }
                .map { it.toDTO() }
        }
    }

    fun getByPriceRange(min: Double, max: Double): List<ProductDTO> {
        checkDatabaseConnection() // ← ADD THIS
        return transaction {
            ProductEntity.find {
                Products.price greaterEq min.toBigDecimal() and (Products.price lessEq max.toBigDecimal())
            }.map { it.toDTO() }
        }
    }

    fun getLatest(limit: Int): List<ProductDTO> {
        checkDatabaseConnection() // ← ADD THIS
        return transaction {
            ProductEntity.all()
                .orderBy(Products.createdAt to SortOrder.DESC)
                .limit(limit)
                .map { it.toDTO() }
        }
    }

    fun paginate(offset: Int, limit: Int): List<ProductDTO> {
        checkDatabaseConnection() // ← ADD THIS
        return transaction {
            ProductEntity.all()
                .limit(limit, offset.toLong())
                .map { it.toDTO() }
        }
    }

    fun getProductVariantsWithProductInfo(): List<ProductVariantDTO> = transaction {
        checkDatabaseConnection()
        ProductVariantEntity.all().map { it.toDTO() }
    }

    fun deleteProduct(id: Int, deletedBy: Int? = null): Boolean {
        checkDatabaseConnection() // ← ADD THIS
        return transaction {
            ProductEntity.findById(id)?.let { product ->
                val name = product.name
                product.delete()
                AuditService.log(
                    adminId = deletedBy,
                    action = "DELETE",
                    targetTable = "products",
                    targetId = id
                )
                true
            } ?: false
        }
    }

    private fun checkDatabaseConnection() {
        if (!DbConnection.isConnected()) {
            throw IllegalStateException("Database not connected. Please call Database.connect() first.")
        }
    }
}
