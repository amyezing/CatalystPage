package catalystpage.com.util

import admin.dto.ShippingSummary
import dto.ShippingStatus

fun mapShippingStatusToSummary(status: ShippingStatus): ShippingSummary {
    return when (status) {
        ShippingStatus.Pending -> ShippingSummary.pending
        ShippingStatus.Shipped, ShippingStatus.Delivered -> ShippingSummary.ready
        ShippingStatus.Cancelled -> ShippingSummary.pending // Or handle differently
    }
}
