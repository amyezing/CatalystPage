package catalystpage.com.admin.adashboards

import admin.dto.ShippingDTO
import androidx.compose.runtime.*
import catalystpage.com.admin.adashboards.Table
import catalystpage.com.admin.fetcher.fetchShippingSummary
import catalystpage.com.admin.fetcher.migrateShippingData
import catalystpage.com.admin.fetcher.updateShippingById
import catalystpage.com.database.userShipping
import catalystpage.com.util.Constants
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.WebSocket

@Composable
fun ShippingSummary(toastMessage: MutableState<String?>) {
    var shippingList by remember { mutableStateOf<List<ShippingDTO>>(emptyList()) }
    var isShippingApproved by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Initial load
    LaunchedEffect(Unit) {
        shippingList = fetchShippingSummary()
    }

    // ✅ Listen for global shipping updates
    DisposableEffect(Unit) {
        val socket = WebSocket("wss://${Constants.HOST}/api/ws/shipping/global")

        socket.onmessage = { event ->
            val message = event.data.toString()
            console.log("📦 Shipping update: $message")

            scope.launch {
                shippingList = fetchShippingSummary()
                toastMessage.value = "Shipping data refreshed"
                delay(3000)
                toastMessage.value = null
            }
        }

        socket.onopen = { console.log("✅ Connected to shipping updates") }
        socket.onclose = { console.log("❌ Disconnected from shipping updates") }
        socket.onerror = { console.error("⚠️ WebSocket error on shipping updates") }

        onDispose {
            socket.close()
            console.log("🔒 Closed global WebSocket connection")
        }
    }

    Column {
        P(
            attrs = Modifier
                .margin(bottom = 16.px)
                .fontSize(24.px)
                .toAttrs()
        ) {
            Text("Shipping Quote Management")
        }

        // 🚀 Migrate Button
        Button(
            attrs = Modifier
                .margin(bottom = 16.px)
                .onClick {
                    scope.launch {
                        try {
                            migrateShippingData()
                            toastMessage.value = "Migration triggered successfully"
                            shippingList = fetchShippingSummary() // Refresh after migration
                        } catch (e: Exception) {
                            toastMessage.value = "Migration failed"
                            console.error("Migration failed", e)
                        }
                        delay(3000)
                        toastMessage.value = null
                    }
                }
                .toAttrs()
        ) {
            Text("Migrate Shipping Data")
        }

        if (shippingList.isEmpty()) {
            Text("Loading shipping summary...")
        } else {
            Table(attrs = Modifier.classNames("table", "table-striped").toAttrs()) {
                Thead {
                    Tr {
                        listOf(
                            "Order ID",
                            "Courier",
                            "Scheduled Date",
                            "Tracking Number",
                            "Fee (₱)",
                            "Notes",
                            "Actions"
                        ).forEach {
                            Th(attrs = Modifier.padding(8.px).toAttrs()) { Text(it) }
                        }
                    }
                }

                Tbody {
                    shippingList.forEach { shipping ->
                        var courier by remember { mutableStateOf(shipping.courier ?: "") }
                        var scheduledDate by remember { mutableStateOf(shipping.scheduledDate ?: "") }
                        var trackingNumber by remember { mutableStateOf(shipping.trackingNumber ?: "") }
                        var shippingFee by remember { mutableStateOf(shipping.shippingFee.toString()) }

                        Tr {
                            // Order ID
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Text(shipping.orderId.toString())
                            }

                            // Courier
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Text(courier.ifBlank { "No courier assigned" })
                            }

                            // Scheduled Date
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Input(InputType.Date) {
                                    value(scheduledDate)
                                    onInput { scheduledDate = it.value }
                                }
                            }

                            // Tracking Number
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Input(InputType.Text) {
                                    value(trackingNumber)
                                    onInput { trackingNumber = it.value }
                                }
                            }

                            // Shipping Fee (editable)
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Input(InputType.Number) {
                                    value(shippingFee.ifBlank { "0.00" })
                                    onInput { e -> shippingFee = e.value.toString() }
                                    attr("step", "0.01")
                                    attr("min", "0")
                                }
                            }

                            // Notes (readonly)
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Text(shipping.notes ?: "")
                            }

                            // Save Button
                            Td(attrs = Modifier.padding(8.px).toAttrs()) {
                                Button(
                                    attrs = Modifier.onClick {
                                        scope.launch {
                                            try {
                                                updateShippingById(
                                                    shipping.orderId,
                                                    ShippingDTO(
                                                        id = shipping.id,
                                                        orderId = shipping.orderId,
                                                        courier = courier,
                                                        scheduledDate = scheduledDate,
                                                        trackingNumber = trackingNumber,
                                                        shippingFee = shippingFee.toDoubleOrNull() ?: 0.0,
                                                        notes = shipping.notes,
                                                        createdAt = shipping.createdAt,
                                                        updatedAt = shipping.updatedAt,
                                                        status = shipping.status
                                                    )
                                                )

                                                val response = userShipping.get("/api/admin/shipping/${shipping.orderId}")
                                                val adminData = response.body<ShippingDTO>()

                                                if (!adminData.trackingNumber.isNullOrBlank() && adminData.shippingFee > 0) {
                                                    isShippingApproved = true
                                                    toastMessage.value = "Shipping approved for Order #${shipping.orderId}"
                                                } else {
                                                    isShippingApproved = false
                                                    toastMessage.value = "Shipping updated, waiting for admin approval"
                                                }

                                                delay(3000)
                                                toastMessage.value = null
                                            } catch (e: Exception) {
                                                toastMessage.value =
                                                    "Failed to update shipping for Order #${shipping.orderId}"
                                                console.error("Update failed", e)
                                            }
                                        }
                                    }.toAttrs()
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
