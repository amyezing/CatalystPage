package catalystpage.com.service

import catalystpage.com.entity.LabelEntity
import catalystpage.com.model.ProductLabels
import dto.LabelDTO
import dto.ProductDTO
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction

object LabelService {
    fun getAll(): List<LabelDTO> = transaction {
        LabelEntity.all().map { it.toDTO() }
    }

    fun add(label: LabelDTO): LabelDTO = transaction {
        LabelEntity.new {
            name = label.name
        }.toDTO()
    }

    fun update(id: Int, label: LabelDTO): LabelDTO? = transaction {
        val entity = LabelEntity.findById(id) ?: return@transaction null
        entity.name = label.name
        entity.toDTO()
    }

    fun delete(id: Int) = transaction {
        LabelEntity.findById(id)?.delete()
    }

    fun addLabelToProduct(productId: Int, labelId: Int) = transaction {
        ProductLabels.insertIgnore {
            it[ProductLabels.product] = productId
            it[ProductLabels.label] = labelId
        }
    }

    fun removeLabelFromProduct(productId: Int, labelId: Int) = transaction {
        ProductLabels.deleteWhere {
            (ProductLabels.product eq productId) and (ProductLabels.label eq labelId)
        }
    }

    fun getProductsByLabel(labelId: Int): List<ProductDTO> = transaction {
        LabelEntity.findById(labelId)
            ?.products
            ?.map { it.toDTO() }
            ?: emptyList()
    }
}