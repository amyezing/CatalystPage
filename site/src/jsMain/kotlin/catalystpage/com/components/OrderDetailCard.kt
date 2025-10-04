package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.dashboards.ProfileRow
import catalystpage.com.database.CheckoutService
import catalystpage.com.database.CheckoutService.getProductInfoByVariantId
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Constants.ROBOTO_SERIF
import catalystpage.com.util.Res
import catalystpage.com.util.formatCurrency
import catalystpage.com.util.formatReadableDate
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.CartItemDTO
import dto.OrderDTO
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.*

@Composable
fun OrderDetailsCard(
    orderId: Int,
    firebaseUid: String,
    onBack: () -> Unit
) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.SM

    var order by remember { mutableStateOf<OrderDTO?>(null) }
    var items by remember { mutableStateOf<List<CartItemDTO>>(emptyList()) }
    var shipping by remember { mutableStateOf<String?>(null) }
    var paymentStatus by remember { mutableStateOf("Pending") }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        try {
            isLoading = true
            val orders = CheckoutService.getUserOrders(firebaseUid)
            order = orders.firstOrNull { it.id == orderId }

            if (order != null) {
                coroutineScope {
                    launch {
                        val rawItems = CheckoutService.getCartItemsByOrderId(orderId)
                        items = rawItems.map { item ->
                            val product = item.productVariantId?.let { getProductInfoByVariantId(it) }
                            val variant = product?.variants?.find { it.id == item.productVariantId }

                            item.copy(
                                productName = product?.name ?: "Unknown Product",
                                price = variant?.price ?: item.price,
                                packSize = variant?.packSize
                            )
                        }
                    }
                    launch { shipping = CheckoutService.getShippingDetails(orderId) }
                    launch { paymentStatus = CheckoutService.fetchPaymentStatus(orderId) }
                }
            }
            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load order: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(900.px)
            .margin(top = 20.px, bottom = 20.px)
            .padding(20.px)
            .borderRadius(15.px)
            .backgroundColor(rgba(255, 255, 255, 0.5))
            .classNames("print-center"),
        verticalArrangement = Arrangement.spacedBy(16.px)
    ) {
        // --- Buttons Row / Column ---
        if (isMobile) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BackButton(onBack)
                PrintButton()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .justifyContent(JustifyContent.SpaceBetween)
                    .alignItems(AlignItems.Center)
            ) {
                BackButton(onBack)
                PrintButton()
            }
        }

        when {
            isLoading -> P { Text("Loading order…") }
            error != null -> P(attrs = Modifier.color(Colors.Red).toAttrs()) { Text(error!!) }
            order == null -> P { Text("Order not found.") }
            else -> {
                // --- Header ---
                if (isMobile) {
                    Column(
                        modifier = Modifier.fillMaxWidth().textAlign(textAlign = TextAlign.Center),
                        verticalArrangement = Arrangement.spacedBy(8.px),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Img(
                            src = Res.Image.logo,
                            attrs = Modifier.height(40.px).toAttrs()
                        )
                        H2(attrs = Modifier.fontFamily(FONT_FAMILY).fontSize(18.px).toAttrs()) {
                            Text("Catalyst Beverage")
                        }
                        H3(attrs = Modifier.fontSize(14.px).toAttrs()) {
                            Text("Order #${order!!.id}")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .margin(bottom = 20.px)
                            .classNames("print-header")
                            .justifyContent(JustifyContent.SpaceBetween)
                            .alignItems(AlignItems.Center)
                    ) {
                        Row(modifier = Modifier.alignItems(AlignItems.Center)) {
                            Img(
                                src = Res.Image.logo,
                                attrs = Modifier.height(40.px).margin(right = 12.px).toAttrs()
                            )
                            H2(
                                attrs = Modifier
                                    .fontFamily(FONT_FAMILY)
                                    .fontSize(20.px)
                                    .fontWeight(FontWeight.Bold)
                                    .margin(0.px)
                                    .toAttrs()
                            ) { Text("Catalyst Beverage Manufacturing") }
                        }
                        H3(
                            attrs = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontWeight(FontWeight.Bold)
                                .fontSize(16.px)
                                .margin(0.px)
                                .toAttrs()
                        ) { Text("Order #${order!!.id}") }
                    }
                }

                // --- Order Info Table ---
                Table(
                    attrs = Modifier
                        .borderRadius(r = 10.px)
                        .fillMaxWidth()
                        .fontFamily(FONT_FAMILY)
                        .fontSize(if (isMobile) 12.px else 14.px)
                        .classNames("table", "table-striped", "table-sm")
                        .margin(bottom = 16.px)
                        .toAttrs()
                ) {
                    Tbody {
                        OrderRow("Placed:", formatReadableDate(order!!.createdAt) ?: "-")
                        OrderRow("Total:", formatCurrency(order!!.totalPrice), bold = true)
                        OrderRow("Status:", order!!.status)
                        OrderRow("Payment:", paymentStatus)
                        OrderRow("Address:", shipping ?: "Loading…")
                    }
                }

                // --- Items ---
                H4(
                    attrs = Modifier
                        .margin(top = 20.px, bottom = 8.px)
                        .fontSize(if (isMobile) 14.px else 16.px)
                        .fontFamily(FONT_FAMILY)
                        .fontWeight(FontWeight.Bold)
                        .toAttrs()
                ) { Text("Items") }

                if (items.isEmpty()) {
                    P { Text("No items found.") }
                } else {
                    Table(
                        attrs = Modifier
                            .fillMaxWidth()
                            .fontFamily(FONT_FAMILY)
                            .fontSize(if (isMobile) 12.px else 14.px)
                            .classNames("table", "table-striped", "text-center")
                            .toAttrs()
                    ) {
                        Thead {
                            Tr {
                                Th(attrs = Modifier.padding(if (isMobile) 4.px else 8.px).toAttrs()) { Text("Quantity") }
                                Th(attrs = Modifier.padding(if (isMobile) 4.px else 8.px).toAttrs()) { Text("Item") }
                                Th(attrs = Modifier.padding(if (isMobile) 4.px else 8.px).toAttrs()) { Text("Subtotal") }
                            }
                        }
                        Tbody {
                            items.forEach { item ->
                                Tr {
                                    Td(attrs = Modifier.padding(if (isMobile) 4.px else 8.px).toAttrs()) { Text(item.quantity.toString()) }
                                    Td(attrs = Modifier.padding(if (isMobile) 4.px else 8.px).toAttrs()) { Text(item.productName ?: "Unnamed Item") }
                                    Td(attrs = Modifier.padding(if (isMobile) 4.px else 8.px).toAttrs()) {
                                        Text(formatCurrency(item.price?.times(item.quantity) ?: 0.0))
                                    }
                                }
                            }
                        }
                    }
                }

                // --- Footer Note ---
                Div(
                    attrs = Modifier
                        .fillMaxWidth()
                        .classNames("print-note")
                        .margin(top = if (isMobile) 24.px else 40.px)
                        .toAttrs()
                ) {
                    P(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .textAlign(textAlign = TextAlign.Center)
                            .fontSize(if (isMobile) 12.px else 14.px)
                            .margin(bottom = if (isMobile) 4.px else 8.px)
                            .toAttrs()
                    ) {
                        Text("© 2025 Catalyst Beverage Manufacturing. All rights reserved.")
                    }

                    P(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .textAlign(textAlign = TextAlign.Center)
                            .fontSize(if (isMobile) 12.px else 14.px)
                            .margin(0.px)
                            .toAttrs()
                    ) {
                        Text("This document is valid without signature. For support, email: ")
                        A(
                            href = "mailto:kombucha@catalystbeveragemanufacturing.com",
                            attrs = Modifier
                                .fontFamily(ROBOTO_SERIF)
                                .color(Colors.Black)
                                .textDecorationLine(TextDecorationLine.Underline)
                                .toAttrs()
                        ) {
                            Text("kombucha@catalystbeveragemanufacturing.com")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.SM

    Button(
        attrs = Modifier
            .id("printButton")
            .width(if (isMobile) 150.px else 180.px)
            .margin { if (isMobile) bottom(10.px) else bottom(0.px) }
            .fontSize(14.px)
            .fontFamily(FONT_FAMILY)
            .borderRadius(8.px)
            .backgroundColor(rgba(0, 0, 0, 0.2))
            .color(Colors.Black)
            .padding(8.px, 6.px)
            .onClick { onBack() }
            .toAttrs()
    ) {
        if (isMobile) {
            Text("Back") // 👈 only arrow on mobile
        } else {
            Text("Back to My Orders") // 👈 full text on desktop
        }
    }
}

@Composable
private fun PrintButton() {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.SM

    Button(
        attrs = Modifier
            .id("printButton")
            .width(if (isMobile) 150.px else 180.px)
            .fontSize(14.px)
            .fontFamily(FONT_FAMILY)
            .borderRadius(8.px)
            .backgroundColor(rgba(0, 0, 0, 0.6))
            .color(Colors.White)
            .padding(8.px, 6.px)
            .onClick { window.print() }
            .toAttrs()
    ) {
        if (isMobile) {
            Text("Print") // 👈 only printer icon on mobile
        } else {
            Text("Print / Save as PDF") // 👈 full label on desktop
        }
    }
}

@Composable
private fun OrderRow(label: String, value: String, bold: Boolean = false) {
    Tr {
        Td(attrs = Modifier.fontWeight(FontWeight.Bold).padding(6.px).toAttrs()) { Text(label) }
        Td(attrs = Modifier.padding(6.px).fontWeight(if (bold) FontWeight.Bold else FontWeight.Normal).toAttrs()) {
            Text(value)
        }
    }
}
