package catalystpage.com.admin.adashboards

import admin.dto.AdminOrderDTO
import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.fetchAdminOrderSummary
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Composable
fun OrderSummary() {
    var orders by remember { mutableStateOf<List<AdminOrderDTO>>(emptyList()) }

    LaunchedEffect(Unit) {
        orders = fetchAdminOrderSummary()
    }

    if (orders.isEmpty()) {
        Text("Loading order summary...")
    } else {
        Column {
            P(
                attrs = Modifier
                    .margin(bottom = 16.px)
                    .fontSize(24.px)
                    .toAttrs()
            ) {
                Text("Admin Order Summary")
            }

            // 🔹 Optionally make this a table-like layout
            Table(attrs = Modifier.classNames("table", "table-striped").toAttrs()) {
                Thead {
                    Tr {
                        listOf("Order #", "Email", "Total", "Status", "Date", "Address").forEach { label ->
                            Th(attrs = Modifier.padding(8.px).toAttrs()) { Text(label) }
                        }
                    }
                }
                Tbody {
                    orders.forEach { order ->
                        Tr {
                            Td(attrs = Modifier.padding(8.px).toAttrs()) { Text("#${order.id}") }
                            Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(order.userEmail) }
                            Td(attrs = Modifier.padding(8.px).toAttrs()) { Text("₱${order.totalPrice}") }
                            Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(order.status) }
                            Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(order.createdAt) }
                            Td(attrs = Modifier.padding(8.px).toAttrs()) { Text(order.address ?: "N/A") }
                        }
                    }
                }
            }
        }
    }
}
