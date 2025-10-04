package catalystpage.com.admin.adashboards

import androidx.compose.runtime.*
import catalystpage.com.database.JsProductFetcher
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import dto.LabelDTO
import dto.ProductDTO
import dto.ProductVariantDTO
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.files.get

@Composable
fun ProductManager() {
    var products by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }
    var labels by remember { mutableStateOf<List<LabelDTO>>(emptyList()) }
    var selectedLabels by remember { mutableStateOf<Set<Int>>(emptySet()) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageFile by remember { mutableStateOf<org.w3c.files.File?>(null) }
    var price by remember { mutableStateOf("") }

    var variants by remember { mutableStateOf<List<ProductVariantDTO>>(emptyList()) }

    val fetcher = remember { JsProductFetcher() }
    val scope = rememberCoroutineScope()

    // Load all products & labels
    LaunchedEffect(Unit) {
        products = fetcher.fetchProducts()
        labels = fetcher.fetchLabels()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.px)) {
        H2 { Text("Product Manager") }

        // --- Product Inputs ---
        Input(InputType.Text) {
            value(name)
            placeholder("Name")
            onInput { name = it.value }
        }

        Input(InputType.Text) {
            value(description)
            placeholder("Description")
            onInput { description = it.value }
        }

        Input(InputType.Number) {
            value(price)
            placeholder("Price")
            onInput { price = it.value.toString() }
        }

        Input(InputType.File) {
            onChange { ev ->
                val input = ev.target as? org.w3c.dom.HTMLInputElement
                val files = input?.files
                if (files != null && files.length > 0) {
                    imageFile = files[0]
                }
            }
        }

        // --- Label Selection ---
        H3 { Text("Assign Labels") }
        labels.forEach { label ->
            val isSelected = label.id in selectedLabels
            Row {
                Input(InputType.Checkbox) {
                    checked(isSelected)
                    onChange {
                        selectedLabels =
                            if (isSelected) selectedLabels - label.id
                            else selectedLabels + label.id
                    }
                }
                Span { Text(label.name) }
            }
        }

        // --- Variant Inputs ---
        H3 { Text("Add Variants (Pack Sizes 4, 8, 12, 24)") }

        val predefinedQuantities = listOf(4, 8, 12, 24)
        predefinedQuantities.forEach { qty ->
            var vPrice by remember { mutableStateOf("") }
            var vStock by remember { mutableStateOf("") }

            Row(Modifier.margin(bottom = 8.px)) {
                Span { Text("Qty: $qty") }
                Input(InputType.Number) {
                    value(vPrice)
                    placeholder("Price for $qty")
                    onInput { vPrice = it.value.toString() }
                }
                Input(InputType.Number) {
                    value(vStock)
                    placeholder("Stock")
                    onInput { vStock = it.value.toString() }
                }
                Button(attrs = Modifier.onClick {
                    if (vPrice.isNotBlank()) {
                        variants = variants + ProductVariantDTO(
                            id = 0,
                            productId = 0, // backend assigns
                            quantity = qty,
                            price = vPrice.toDouble(),
                            stock = vStock.toIntOrNull() ?: 0,
                            size = "250mL"
                        )
                    }
                }.toAttrs()) {
                    Text("Add")
                }
            }
        }

        // --- Variants Preview ---
        if (variants.isNotEmpty()) {
            H4 { Text("Variants Preview:") }
            Ul {
                variants.forEach { v ->
                    Li {
                        Text("Qty: ${v.quantity}, Price: ${v.price}, Stock: ${v.stock}, Size: ${v.size ?: "-"}")
                    }
                }
            }
        }

        // --- Submit Product ---
        Button(attrs = Modifier.onClick {
            scope.launch {

                val selectedLabelDTOs = labels.filter { it.id in selectedLabels }
                try {
                    val newProduct = ProductDTO(
                        id = 0,
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        imageUrl = null,
                        variants = variants,
                        labels = selectedLabelDTOs // 🔹 include labels
                    )
                    val created = fetcher.addProduct(newProduct, imageFile)
                    products = products + created

                    // Reset form
                    name = ""
                    description = ""
                    price = ""
                    imageFile = null
                    variants = emptyList()
                    selectedLabels = emptySet()
                } catch (e: Exception) {
                    console.error("Failed to add product", e)
                }
            }
        }.toAttrs()) {
            Text("Add Product")
        }

        // --- Product List ---
        H3 { Text("Product List") }
        Ul {
            products.forEachIndexed { index, product ->
                Li {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${product.name} - ${product.price}")
                            if (product.imageUrl != null) {
                                Img(
                                    src = product.imageUrl,
                                    alt = product.name,
                                    attrs = Modifier.width(100.px).toAttrs()
                                )
                            }
                            if (product.labels.isNotEmpty()) {
                                P { Text("Labels: ${product.labels.joinToString()}") }
                            }
                            if (product.variants.isNotEmpty()) {
                                Ul {
                                    product.variants.forEach { v ->
                                        Li {
                                            Text("Qty: ${v.quantity}, Price: ${v.price}, Stock: ${v.stock}, Size: ${v.size ?: "-"}")
                                        }
                                    }
                                }
                            }
                        }



                        // --- Availability Toggle ---
                        Column {
                            Input(InputType.Checkbox) {
                                checked(product.isAvailable)
                                onChange {
                                    val updatedProduct = product.copy(isAvailable = it.value)
                                    scope.launch {
                                        try {
                                            val updated = fetcher.updateProduct(updatedProduct)
                                            // update local state
                                            products = products.toMutableList().also { list ->
                                                list[index] = updated
                                            }
                                        } catch (e: Exception) {
                                            console.error("Failed to update availability", e)
                                        }
                                    }
                                }
                            }
                            Span { Text(if (product.isAvailable) "Available" else "Unavailable") }
                        }
                    }
                }
            }
        }
    }
}
