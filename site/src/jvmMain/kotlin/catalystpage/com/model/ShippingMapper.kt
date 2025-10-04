package catalystpage.com.model

import catalystpage.com.entity.ShippingDetailsEntity
import dto.ShippingDetailsDTO

fun ShippingDetailsEntity.toDTO(): ShippingDetailsDTO = ShippingDetailsDTO(
    id = id.value,
    orderId = order.id.value,
    address = address,
    courier = courier,
    trackingNumber = trackingNumber,
    status = status,
    shippedAt = shippedAt?.toString(),
    deliveredAt = deliveredAt?.toString()
)