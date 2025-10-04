package model

import dto.LabelDTO
import dto.ProductDTO
import kotlinx.serialization.Serializable

@Serializable
data class ProductWithLabelsDTO(
    val product: ProductDTO,
    val labels: List<LabelDTO> = emptyList()
)