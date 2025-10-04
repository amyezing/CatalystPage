package model

data class CreateShippingDetailsRequest(
    val orderId: Int,
    val address: String
)