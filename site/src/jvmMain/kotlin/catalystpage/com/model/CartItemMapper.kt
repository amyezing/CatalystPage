package catalystpage.com.model

import catalystpage.com.entity.CartItemEntity
import dto.CartItemDTO

fun CartItemEntity.toDTO() = CartItemDTO(
    id = id.value,
    firebaseUid = firebaseUid,
    quantity = quantity,
    productVariantId = productVariantId
)