package catalystpage.com.dashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.fetchShippingSummaryByOrderId
import catalystpage.com.database.CheckoutService.cancelOrder
import catalystpage.com.database.CheckoutService.getCartItemsByOrderId
import catalystpage.com.database.CheckoutService.getProductInfoByVariantId
import catalystpage.com.database.CheckoutService.getShippingDetails
import catalystpage.com.database.JsPaymentFetcher
import catalystpage.com.database.JsPaymentFetcher.getPaymentStatus
import catalystpage.com.styles.ButtonStyles
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Constants.ROBOTO_SERIF
import catalystpage.com.util.formatCurrency
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.argb
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.rgba
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.FaSpinner
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.CartItemDTO
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.files.File


@Composable
fun CheckoutPage(
    orderId: Int,
    firebaseUid: String,
    onBackToCart: () -> Unit
) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.SM
    val containerWidth = when {
        isMobile -> 100.percent
        breakpoint <= Breakpoint.MD -> 90.percent
        else -> 80.percent
    }

    var enrichedCartItems by remember { mutableStateOf<List<CartItemDTO>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var uploadSuccess by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf<String?>(null) }
    var paymentStatus by remember { mutableStateOf("LOADING") }
    var shippingFee by remember { mutableStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(orderId) {
        val rawCartItems = getCartItemsByOrderId(orderId)
        address = getShippingDetails(orderId)
        paymentStatus = getPaymentStatus(orderId)
        val shippingSummary = fetchShippingSummaryByOrderId(orderId)
        shippingFee = shippingSummary?.shippingFee ?: 0.0
        enrichedCartItems = rawCartItems.map { item ->
            val product = item.productVariantId?.let { getProductInfoByVariantId(it) }
            val variant = product?.variants?.find { it.id == item.productVariantId }
            item.copy(
                productName = product?.name ?: "Unknown Product",
                price = variant?.price ?: 0.0,
                packSize = variant?.packSize
            )
        }
    }

    if (enrichedCartItems.isEmpty()) {
        Text("Loading checkout data...")
        return
    }

    val subtotalAmount = enrichedCartItems.sumOf { (it.price ?: 0.0) * it.quantity }
    val totalAmount = subtotalAmount + shippingFee
    val formattedSubtotal = formatCurrency(subtotalAmount)
    val formattedShippingFee = formatCurrency(shippingFee)
    val formattedTotal = formatCurrency(totalAmount)

    Box(
        modifier = Modifier
            .fillMaxWidth(containerWidth)
            .borderRadius(r = 15.px)
            .backgroundColor(rgba(255, 255, 255, 0.6))
            .maxWidth(1600.px),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.px)) {
            // Header
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fillMaxWidth()
                    .borderRadius(topRight = 15.px, topLeft = 15.px)
                    .padding(10.px)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Bold)
                    .margin(bottom = 12.px)
                    .toAttrs()
            ) { Text("Checkout Summary") }

            // Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    attrs = ButtonStyles.toModifier()
                        .margin(bottom = 10.px)
                        .borderRadius(r = 10.px)
                        .fontFamily(FONT_FAMILY)
                        .onClick {
                            coroutineScope.launch {
                                cancelOrder(orderId) {
                                    println("Order deleted, returning to cart")
                                    onBackToCart()
                                    window.location.reload()
                                }
                            }
                        }.toAttrs()
                ) {
                    Text("Back to Cart")
                }
            }

            P(attrs = Modifier.fontFamily(FONT_FAMILY).margin(bottom = 10.px).toAttrs()) {
                Text("Order #00$orderId")
            }

            // ----- CART ITEMS -----
            if (isMobile) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.px)
                ) {
                    enrichedCartItems.forEach { item ->
                        val name = item.productName ?: "Unknown Item"
                        val unitPrice = item.price ?: 0.0
                        val subtotal = unitPrice * item.quantity
                        val formattedUnit = formatCurrency(unitPrice)
                        val formattedItemSubtotal = formatCurrency(subtotal)
                        val pack = item.packSize?.toString() ?: "-"

                        val rows = listOf(
                            "Qty" to item.quantity.toString(),
                            "Item" to name,
                            "Pack" to pack,
                            "Price" to formattedUnit,
                            "Subtotal" to formattedItemSubtotal
                        )

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
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rows.forEachIndexed { index, (label, value) ->
                                    val rowColor = if (index % 2 == 0) rgba(245, 245, 245, 1f) else rgba(255, 255, 255, 1f)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .backgroundColor(rowColor)
                                            .padding(6.px, 4.px),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        P(attrs = Modifier.fontWeight(FontWeight.Bold).toAttrs()) { Text(label) }
                                        P(attrs = Modifier.fontFamily(FONT_FAMILY).toAttrs()) { Text(value) }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Desktop/tablet: full table
                Div(
                    attrs = Modifier
                        .borderRadius(r = 15.px)
                        .border(
                            style = LineStyle.Solid,
                            color = Colors.PaleTurquoise,
                            width = 0.5.px
                        )
                        .classNames("overflow-hidden", "mb-4")
                        .toAttrs()
                ) {
                    Table(
                        attrs = Modifier.classNames(
                            "table-sm",
                            "table-striped",
                            "align-middle",
                            "text-center"
                        ).toAttrs()
                    ) {
                        Thead(
                            attrs = Modifier.fontFamily(FONT_FAMILY)
                                .borderBottom(width = 0.1.px, color = Colors.PaleTurquoise, style = LineStyle.Ridge)
                                .classNames("table", "table-striped", "text-center").toAttrs()
                        ) {
                            Tr {
                                listOf("Qty", "Item", "Pack", "Unit", "Subtotal").forEach { label ->
                                    Th(
                                        attrs = Modifier.classNames("px-3", "py-2").toAttrs {
                                            attr("scope", "col")
                                        }
                                    ) { Text(label) }
                                }
                            }
                        }
                        Tbody {
                            enrichedCartItems.forEach { item ->
                                val name = item.productName ?: "Unknown Item"
                                val unitPrice = item.price ?: 0.0
                                val subtotal = unitPrice * item.quantity
                                val formattedUnit = formatCurrency(unitPrice)
                                val formattedItemSubtotal = formatCurrency(subtotal)
                                val pack = item.packSize?.toString() ?: "-"
                                Tr(attrs = Modifier.fontFamily(FONT_FAMILY).toAttrs()) {
                                    Td(attrs = Modifier.classNames("px-3", "py-2").toAttrs()) { Text(item.quantity.toString()) }
                                    Td(attrs = Modifier.classNames("px-3", "py-2", "fw-bold").toAttrs()) { Text(name) }
                                    Td(attrs = Modifier.classNames("px-3", "py-2").toAttrs()) { Text(pack) }
                                    Td(attrs = Modifier.classNames("px-3", "py-2").toAttrs()) { Text(formattedUnit) }
                                    Td(attrs = Modifier.classNames("px-3", "py-2").toAttrs()) { Text(formattedItemSubtotal) }
                                }
                            }
                        }
                    }
                }
            }

            // ----- TOTALS -----
            Table(
                attrs = Modifier
                    .classNames("table", "table-striped", "text-start")
                    .width(100.percent)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(if (isMobile) 12.px else 14.px)
                    .toAttrs()
            ) {
                Tbody {
                    listOf(
                        "Subtotal" to formattedSubtotal,
                        "Shipping Fee" to formattedShippingFee,
                        "Grand Total" to formattedTotal
                    ).forEachIndexed { index, (label, value) ->
                        Tr {
                            Td(attrs = Modifier.fontWeight(FontWeight.Bold).toAttrs()) { Text(label) }
                            Td(attrs = Modifier.fontWeight(if (index == 2) FontWeight.Bold else FontWeight.Normal).toAttrs()) { Text(value) }
                        }
                    }
                    Tr {
                        Td(attrs = Modifier.fontWeight(FontWeight.Bold).toAttrs()) { Text("Shipping to") }
                        Td { Text(address ?: "-") }
                    }
                }
            }
            PaymentQRSection(totalAmount = totalAmount)
            // File input
            Div(attrs = Modifier.classNames("mb-3").toAttrs()) {
                Input(
                    type = InputType.File,
                    attrs = ButtonStyles.toModifier()
                        .classNames("form-control")
                        .cursor(Cursor.Pointer)
                        .toAttrs {
                            onInput { event ->
                                val fileInput = event.target
                                selectedFile = fileInput.files?.item(0)
                            }
                        }
                )
            }

            // Upload button
            Button(
                attrs = ButtonStyles.toModifier()
                    .margin(top = 10.px, bottom = 10.px)
                    .maxWidth(150.px)
                    .fontSize(14.px)
                    .borderRadius(r = 15.px)
                    .cursor(Cursor.Pointer)
                    .fontFamily(FONT_FAMILY)
                    .onClick {
                        coroutineScope.launch {
                            if (selectedFile != null) {
                                JsPaymentFetcher.uploadPaymentProof(orderId, selectedFile!!) { status ->
                                    uploadSuccess = status.startsWith("✅")
                                    message = status
                                }
                            } else {
                                message = "⚠️ Please select a file first."
                            }
                        }
                    }
                    .toAttrs()
            ) { Text("Upload Payment Proof") }

            if (uploadSuccess) {
                P(
                    attrs = Modifier
                        .fontFamily(ROBOTO_SERIF)
                        .color(Colors.Black)
                        .margin(top = 8.px)
                        .toAttrs()
                ) {
                    Text("Payment uploaded! Awaiting admin approval...")
                }
            }
            message?.let {
                P(
                    attrs = Modifier
                        .color(Colors.Black)
                        .fontFamily(ROBOTO_SERIF)
                        .margin(top = 8.px)
                        .toAttrs()
                ) { Text(it) }
            }
        }
    }
}



@Composable
fun PaymentQRSection(totalAmount: Double) {
    var showQR by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column {
        if (!showQR) {
            Box(
                modifier = Modifier
                    .position(Position.Relative)
                    .maxWidth(150.px)
            ) {
                // Button underneath
                Button(
                    attrs = ButtonStyles.toModifier()
                        .margin(topBottom = 12.px)
                        .width(150.px)
                        .fontSize(16.px)
                        .borderRadius(r = 10.px)
                        .fontFamily(FONT_FAMILY)
                        .onClick {
                            loading = true
                            coroutineScope.launch {
                                delay(1000) // 🌀 Simulate loading
                                loading = false
                                showQR = true
                            }
                        }
                        .toAttrs()
                ) {
                    Text("Generate QR code")
                }

                // Spinner overlay
                if (loading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .position(Position.Absolute)
                            .top(0.px)
                            .left(0.px)
                            .backgroundColor(argb(0.3f, 255, 255, 255)) // optional semi-transparent layer
                    ) {
                        FaSpinner(size = IconSize.SM)
                    }
                }
            }
        }

        if (showQR && !loading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .backgroundColor(argb(a = 0.2f, r = 0, g = 0, b = 0))
                    .padding(16.px)
                    .fontFamily(FONT_FAMILY)
                    .margin(bottom = 10.px)
                    .borderRadius(12.px)
            ) {
                Text("Scan this QR to pay via GCash")
                Img(
                    src = "https://storage.googleapis.com/catalyst-cloud-storage/gcash-crop.jpg",
                    attrs = {
                        attr("alt", "GCash QR Code")
                        attr("oncontextmenu", "return false") // 🔒 Disable right-click
                        style {
                            property("width", "180px")
                            property("margin-top", "12px")
                            property("border-radius", "8px")
                        }
                    }
                )
                P(attrs = Modifier.fontFamily(FONT_FAMILY).fontSize(14.px).toAttrs()) {
                    Text("Enter ₱$totalAmount when prompted")
                }
                P(attrs = Modifier.fontFamily(FONT_FAMILY).fontWeight(FontWeight.Bold).fontSize(13.px).color(Colors.Gray).toAttrs()) {
                    Text("GCash: 09661680090")
                }
            }
        }
    }
}