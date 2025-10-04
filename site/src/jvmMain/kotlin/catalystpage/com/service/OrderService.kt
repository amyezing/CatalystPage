package catalystpage.com.service

import admin.dto.AdminOrderDTO
import catalystpage.com.db.EnvConfig
import catalystpage.com.entity.*
import catalystpage.com.model.*
import dto.CartItemDTO
import dto.CheckoutRequest
import dto.OrderDTO
import dto.ShippingStatus
import model.OrderStatus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object OrderService {

    fun getById(id: Int): OrderDTO? = transaction {
        OrderEntity.findById(id)?.toDTO()
    }
    fun createOrder(request: CheckoutRequest): OrderEntity {
        return transaction {
            // Check for an existing pending order
            val existingPending = OrderEntity.find {
                (Orders.firebaseUid eq request.firebaseUid) and
                        (Orders.status eq OrderStatus.Pending)
            }.firstOrNull()

            if (existingPending != null) {
                error("You already have a pending order. Please complete or cancel it first.")
            }

            // Load cart items
            val cartItems = CartItemEntity.find { CartItems.firebaseUid eq request.firebaseUid }.toList()
            if (cartItems.isEmpty()) error("Cart is empty")

            // Calculate total
            val total = cartItems.sumOf { cartItem ->
                val productVariant = cartItem.productVariantId?.let { id ->
                    ProductVariantEntity.findById(id)
                        ?: error("Product variant $id not found")
                } ?: error("Invalid product variant")

                productVariant.price.toDouble() * cartItem.quantity
            }


            // Create order
            val order = OrderEntity.new {
                this.firebaseUid = request.firebaseUid
                this.totalPrice = total.toBigDecimal()
                this.status = OrderStatus.Pending
            }

            // Add order items + reduce stock
            cartItems.forEach { cartItem ->
                val pv = ProductVariantEntity.findById(cartItem.productVariantId!!)
                    ?: error("Product variant ${cartItem.productVariantId} not found")

                OrderItemEntity.new {
                    this.order = order
                    this.productVariantId = pv.id
                    this.quantity = cartItem.quantity
                    this.price = pv.price
                }

                // Reduce stock
                pv.stock -= cartItem.quantity
            }

            // Create shipping details
            ShippingDetailsEntity.new {
                this.order = order
                this.address = request.address
                this.courier = request.courier
                this.status = ShippingStatus.Pending
            }
            val user = UserEntity.find { Users.firebaseUid eq request.firebaseUid }.firstOrNull()
            user?.email?.let { email ->
                EmailService.sendMail(
                    to = email,
                    subject = "Order Confirmation #${order.id.value}",
                    body = """
                        Hi ${user.name ?: "Customer"},
                        
                        Thank you for your order! 🎉
                        Order ID: ${order.id.value}
                        Total: ₱${order.totalPrice}
                        Status: ${order.status}
                        
                        We'll notify you once it’s processed.
                        
                        – Catalyst Team
                    """.trimIndent()
                )
            }

            EmailService.sendMail(
                to = EnvConfig.smtpUser,
                subject = "New Order Placed (#${order.id.value})",
                body = """
                    A new order has been placed.
                    
                    Order ID: ${order.id.value}
                    Customer: ${user?.email ?: "Unknown"}
                    Total: ₱${order.totalPrice}
                """.trimIndent()
            )
            order
        }
    }




    fun getPendingOrder(firebaseUid: String): OrderEntity? {
        return transaction {
            OrderEntity.find {
                (Orders.firebaseUid eq firebaseUid) and
                        (Orders.status eq OrderStatus.Pending)
            }.firstOrNull()
        }
    }

    fun getOrderItems(orderId: Int): List<CartItemDTO> {
        return transaction {
            println("All Order IDs: ${OrderEntity.all().map { it.id.value }}")

            val order = OrderEntity.findById(orderId) ?: error("Order not found")

            OrderItemEntity.find { OrderItems.order eq order.id }.map { item ->
                CartItemDTO(
                    id = item.id.value,
                    quantity = item.quantity,
                    price = item.price.toDouble(),
                    productVariantId = item.productVariantId?.value,
                    firebaseUid = order.firebaseUid
                )
            }
        }
    }

    fun cancelOrderById(id: Int): Boolean = transaction {
        val order = OrderEntity.findById(id) ?: return@transaction false

        if (order.status == OrderStatus.Pending) {
            order.status = OrderStatus.Cancelled

            // Optional: Cancel shipping
            val shipping = ShippingDetailsEntity.find { ShippingDetails.order eq order.id }.firstOrNull()
            shipping?.status = ShippingStatus.Cancelled  // if such status exists

            true
        } else {
            false // Only pending orders can be cancelled
        }
    }

    fun getAllOrdersWithUserAndShipping(): List<AdminOrderDTO> = transaction {
        (Orders innerJoin Users leftJoin ShippingDetails).selectAll().map { row ->
            AdminOrderDTO(
                id = row[Orders.id].value,
                firebaseUid = row[Orders.firebaseUid],
                userEmail = row[Users.email] ?: "N/A",
                totalPrice = row[Orders.totalPrice].toDouble(),
                status = row[Orders.status].name,
                createdAt = row[Orders.createdAt].toString(),
                address = row[ShippingDetails.address]
            )
        }
    }
    fun getOrdersByUser(uid: String): List<OrderEntity> {
        return transaction {
            OrderEntity.find { Orders.firebaseUid eq uid }
                .orderBy(Orders.createdAt to SortOrder.DESC)
                .toList()
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: OrderStatus) {
        transaction {
            val order = OrderEntity.findById(orderId) ?: error("Order not found")
            order.status = newStatus

            // 🔔 Email user when status updates
            val user = UserEntity.find { Users.firebaseUid eq order.firebaseUid }.firstOrNull()
            user?.email?.let { email ->   // <-- only runs if email is non-null
                EmailService.sendMail(
                    to = email,
                    subject = "Order #${order.id.value} Update",
                    body = """
                    Hi ${user.name ?: "Customer"},
                    
                    Your order #${order.id.value} status has been updated to: ${order.status}.
                    
                    – Catalyst Team
                """.trimIndent()
                )
            }
        }
    }

}