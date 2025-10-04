package catalystpage.com.dashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.fetchShippingSummaryByOrderId
import catalystpage.com.database.CheckoutService.fetchPaymentStatus
import catalystpage.com.database.CheckoutService.getUserOrders
import catalystpage.com.styles.ButtonStyles
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.formatCurrency
import catalystpage.com.util.formatReadableDate
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.OrderDTO
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.*

@Composable
fun MyOrdersSection(firebaseUid: String, onViewOrder: (Int) -> Unit) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.SM

    var orders by remember { mutableStateOf<List<OrderDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(firebaseUid) {
        try {
            orders = getUserOrders(firebaseUid)
            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load orders: ${e.message}"
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .backgroundColor(rgba(255, 255, 255, 0.5))
            .fillMaxWidth(if (breakpoint > Breakpoint.MD) 80.percent else 100.percent)
            .margin(if (breakpoint > Breakpoint.MD) 100.px else 0.px),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.px)) {
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.Bold)
                    .fontSize(20.px)
                    .margin(bottom = 12.px)
                    .toAttrs()
            ) { Text("My Orders") }

            when {
                isLoading -> P { Text("Loading...") }
                error != null -> P(attrs = Modifier.color(Colors.Red).toAttrs()) { Text(error!!) }
                orders.isEmpty() -> P { Text("You haven't placed any orders yet.") }
                else -> {
                    if (isMobile) {
                        // ----- MOBILE: Render as cards -----
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.px)
                        ) {
                            orders.forEach { order ->
                                var paymentStatus by remember(order.id) { mutableStateOf("Loading...") }
                                var shippingFee by remember(order.id) { mutableStateOf(0.0) }
                                var trackingNumber by remember(order.id) { mutableStateOf("") }

                                LaunchedEffect(order.id) {
                                    paymentStatus = fetchPaymentStatus(order.id)
                                    val summary = fetchShippingSummaryByOrderId(order.id)
                                    shippingFee = summary?.shippingFee?.toDouble() ?: 0.0
                                    trackingNumber = summary?.trackingNumber ?: ""
                                }

                                val grandTotal = order.totalPrice + shippingFee

                                Div(
                                    attrs = Modifier
                                        .fillMaxWidth()
                                        .padding(12.px)
                                        .borderRadius(12.px)
                                        .backgroundColor(rgba(255, 255, 255, 0.6))
                                        .boxShadow(blurRadius = 6.px, color = rgba(0, 0, 0, 0.1))
                                        .toAttrs()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.px)
                                    ) {
                                        Table(
                                            attrs = Modifier
                                                .classNames("table", "table-striped", "text-center")
                                                .width(100.percent)
                                                .fontSize(14.px)
                                                .toAttrs()
                                        ) {
                                            Tbody {
                                                Tr {
                                                    Td(attrs = Modifier.fontWeight(FontWeight.Bold).padding(right = 8.px).toAttrs()) {
                                                        Text("Order")
                                                    }
                                                    Td { Text("#${order.id}") }
                                                }
                                                Tr {
                                                    Td(attrs = Modifier.fontWeight(FontWeight.Bold).padding(right = 8.px).toAttrs()) {
                                                        Text("Date")
                                                    }
                                                    Td { Text(formatReadableDate(order.createdAt) ?: "-") }
                                                }
                                                Tr {
                                                    Td(attrs = Modifier.fontWeight(FontWeight.Bold).padding(right = 8.px).toAttrs()) {
                                                        Text("Total")
                                                    }
                                                    Td { Text(formatCurrency(grandTotal)) }
                                                }
                                                Tr {
                                                    Td(attrs = Modifier.fontWeight(FontWeight.Bold).padding(right = 8.px).toAttrs()) {
                                                        Text("Status")
                                                    }
                                                    Td { Text(paymentStatus) }
                                                }
                                                Tr {
                                                    Td(attrs = Modifier.fontWeight(FontWeight.Bold).padding(right = 8.px).toAttrs()) {
                                                        Text("Tracking No.")
                                                    }
                                                    Td { Text(if (trackingNumber.isNotEmpty()) trackingNumber else "-") }
                                                }
                                            }
                                        }

                                        Button(
                                            attrs = Modifier
                                                .fillMaxWidth()
                                                .margin(top = 8.px)
                                                .backgroundColor(rgba(0, 0, 0, 0.6))
                                                .color(Colors.White)
                                                .borderRadius(8.px)
                                                .padding(6.px, 4.px)
                                                .onClick { onViewOrder(order.id) }
                                                .toAttrs()
                                        ) { Text("View") }
                                    }
                                }
                            }
                        }
                    } else {
                        // ----- DESKTOP: Render as table -----
                        Table(
                            attrs = Modifier
                                .fontFamily(FONT_FAMILY)
                                .classNames("table", "table-striped", "text-center")
                                .toAttrs()
                        ) {
                            Thead {
                                Tr {
                                    listOf("Order #", "Date", "Total", "Status", "Tracking Number", "Action").forEach {
                                        Th(attrs = Modifier.padding(8.px).toAttrs()) { Text(it) }
                                    }
                                }
                            }
                            Tbody {
                                orders.forEach { order ->
                                    var paymentStatus by remember(order.id) { mutableStateOf("Loading...") }
                                    var shippingFee by remember(order.id) { mutableStateOf(0.0) }
                                    var trackingNumber by remember(order.id) { mutableStateOf("") }

                                    LaunchedEffect(order.id) {
                                        paymentStatus = fetchPaymentStatus(order.id)
                                        val summary = fetchShippingSummaryByOrderId(order.id)
                                        shippingFee = summary?.shippingFee?.toDouble() ?: 0.0
                                        trackingNumber = summary?.trackingNumber ?: ""
                                    }

                                    val grandTotal = order.totalPrice + shippingFee

                                    Tr {
                                        Td(attrs = Modifier.padding(8.px).toAttrs()) { Text("#${order.id}") }
                                        Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(formatReadableDate(order.createdAt) ?: "-") }
                                        Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(formatCurrency(grandTotal)) }
                                        Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(paymentStatus) }
                                        Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(if (trackingNumber.isNotEmpty()) trackingNumber else "-") }
                                        Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                            Button(
                                                attrs = Modifier
                                                    .backgroundColor(rgba(0, 0, 0, 0.6))
                                                    .color(Colors.White)
                                                    .borderRadius(10.px)
                                                    .fontSize(14.px)
                                                    .padding(6.px, 4.px)
                                                    .fontFamily(FONT_FAMILY)
                                                    .onClick { onViewOrder(order.id) }
                                                    .toAttrs()
                                            ) { Text("View") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
