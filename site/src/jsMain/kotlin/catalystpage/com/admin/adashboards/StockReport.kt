package catalystpage.com.admin.adashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.fetchLowStockReport
import catalystpage.com.admin.fetcher.updateStock
import catalystpage.com.dashboards.TextInputField
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import dto.LowStockItemDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Composable
fun StockReport() {
    var lowStockItems by remember { mutableStateOf<List<LowStockItemDTO>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<LowStockItemDTO?>(null) }

    suspend fun refresh() {
        try {
            lowStockItems = fetchLowStockReport()
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        P(attrs = Modifier.fontSize(12.px).fontWeight(FontWeight.Bold).toAttrs()) {
            Text("📦 Low Stock Items")
        }

        if (errorMessage != null) {
            P(attrs = Modifier.color(Colors.Red).toAttrs()) { Text("❗ Error: $errorMessage") }
        } else if (lowStockItems.isEmpty()) {
            Text("✅ All items are sufficiently stocked.")
        } else {
            Table(lowStockItems, onEditClick = { item -> selectedItem = item })
        }

        selectedItem?.let { item ->
            UpdateStockDialog(
                item = item,
                onUpdate = {
                    selectedItem = null
                    // refresh after updating stock
                    CoroutineScope(Dispatchers.Default).launch {
                        refresh()
                    }
                }
            )
        }
    }
}


@Composable
fun Table(items: List<LowStockItemDTO>, onEditClick: (LowStockItemDTO) -> Unit) {
    Table(attrs = Modifier.classNames("table", "table-striped").toAttrs()
    ) {
        Thead {
            Tr {
                listOf("Type", "Name", "Size", "Pack", "Stock").forEach { label ->
                    Th(attrs = Modifier.padding(8.px).toAttrs()) { Text(label) }
                }
            }
        }
        Tbody {
            items.forEach { item ->
                Tr {
                    Td (attrs = Modifier.padding(8.px).toAttrs()) { Text(item.itemType) }
                    Td (attrs = Modifier.padding(8.px).toAttrs()){ Text(item.itemName) }
                    Td (attrs = Modifier.padding(8.px).toAttrs()){ Text(item.size) }
                    Td (attrs = Modifier.padding(8.px).toAttrs()){ Text(item.quantity.toString()) }
                    Td (attrs = Modifier.padding(8.px).toAttrs()){ Text(item.stock.toString()) }
                    Td(attrs = Modifier.padding(8.px).toAttrs()) {
                        Button(
                            attrs = Modifier
                                .onClick { onEditClick(item) }
                                .toAttrs()
                        ) {
                            Text("Edit")
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun UpdateStockDialog(item: LowStockItemDTO, onUpdate: () -> Unit) {
    var stockInput by remember { mutableStateOf(item.stock.toString()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .backgroundColor(Colors.White)
            .border(1.px, color = Colors.Gray)
            .padding(16.px)
            .maxWidth(300.px)
    ) {
        Text("Update Stock for ${item.itemName}")

        TextInputField(
            value = stockInput,
            onValueChange = { stockInput = it },
            label = ("New Stock")
        )

        Row {
            Button(
                attrs = Modifier
                    .margin(top = 8.px)
                    .onClick {
                        scope.launch {
                            val success = updateStock(item.itemId, stockInput.toIntOrNull() ?: item.stock, item.itemType)
                            if (success) {
                                onUpdate()
                            }
                        }
                    }
                    .toAttrs(),
            )   {
                Text("✅ Update")
            }


            Button(
                attrs = Modifier
                    .margin(top = 8.px)
                    .onClick { onUpdate()}
                    .toAttrs(),
            ) {
                Text("❌ Cancel")
            }
        }
    }
}