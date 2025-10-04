package catalystpage.com.service

import catalystpage.com.model.ProductVariants
import dto.LowStockItemDTO
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object LowStockService {
    fun getAllLowStockItems(): List<LowStockItemDTO> = transaction {
        exec("""
            SELECT item_id, item_name, size, quantity, stock, item_type
            FROM low_stock_items
        """.trimIndent()) { rs ->
            val results = mutableListOf<LowStockItemDTO>()
            while (rs.next()) {
                results.add(
                    LowStockItemDTO(
                        itemId = rs.getInt("item_id"),
                        itemName = rs.getString("item_name"),
                        size = rs.getString("size"),
                        quantity = rs.getInt("quantity"),
                        stock = rs.getInt("stock"),
                        itemType = rs.getString("item_type")
                    )
                )
            }
            results
        } ?: emptyList()
    }
    fun updateStock(itemId: Int, newStock: Int, itemType: String): Boolean = transaction {
        when (itemType.uppercase()) {
            "PRODUCT" -> ProductVariants.update({ ProductVariants.id eq itemId }) {
                it[stock] = newStock
            } > 0

            else -> false
        }
    }
}