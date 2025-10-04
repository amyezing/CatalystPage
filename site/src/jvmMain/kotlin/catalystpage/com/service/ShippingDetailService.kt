package catalystpage.com.service

import catalystpage.com.entity.OrderEntity
import catalystpage.com.entity.ShippingDetailsEntity
import catalystpage.com.model.ShippingDetails
import catalystpage.com.model.toDTO
import catalystpage.com.service.admin.AdminShippingService
import dto.ShippingDetailsDTO
import dto.ShippingStatus
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object ShippingDetailsService {
    fun getAll(): List<ShippingDetailsDTO> = transaction {
        ShippingDetailsEntity.all().map { it.toDTO() }
    }

    fun getByOrderId(orderId: Int): ShippingDetailsDTO? = transaction {
        ShippingDetailsEntity.find { ShippingDetails.order eq orderId }
            .firstOrNull()?.toDTO()
    }



    fun create(orderId: Int, address: String): ShippingDetailsDTO = transaction {
        val order = OrderEntity.findById(orderId) ?: error("Order not found")
        val entity = ShippingDetailsEntity.new {
            this.order = order
            this.address = address
            this.status = ShippingStatus.Pending
        }
        entity.toDTO()
    }



    fun upsertUserShippingDetails(dto: ShippingDetailsDTO): ShippingDetailsDTO = transaction {
        val existing = ShippingDetailsEntity.find { ShippingDetails.order eq dto.orderId }
            .firstOrNull()

        val updated = existing?.apply {
            address = dto.address
            if (dto.courier != null) courier = dto.courier
        } ?: ShippingDetailsEntity.new {
            order = OrderEntity.findById(dto.orderId) ?: error("Order not found")
            address = dto.address
            if (dto.courier != null) courier = dto.courier
            status = ShippingStatus.Pending
        }

        // 🔹 Sync to admin table
        AdminShippingService.upsertAdminShippingSummaryFromUserInput(dto)

        updated.toDTO()
    }


    fun updateStatus(
        orderId: Int,
        status: ShippingStatus,
        trackingNumber: String? = null,
        courier: String? = null
    ): ShippingDetailsDTO? = transaction {
        val shipping = ShippingDetailsEntity.find { ShippingDetails.order eq orderId }
            .firstOrNull() ?: return@transaction null

        shipping.status = status
        if (trackingNumber != null) shipping.trackingNumber = trackingNumber
        if (courier != null) shipping.courier = courier
        if (status == ShippingStatus.Shipped) shipping.shippedAt = LocalDateTime.now()
        if (status == ShippingStatus.Delivered) shipping.deliveredAt = LocalDateTime.now()

        shipping.toDTO()
    }
}