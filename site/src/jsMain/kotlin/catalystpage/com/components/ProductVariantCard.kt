package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.database.JsCartFetcher
import catalystpage.com.database.JsProductFetcher
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Constants.ROBOTO_SERIF
import catalystpage.com.util.formatLabel
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.CartItemDTO
import dto.LabelDTO
import dto.ProductDTO
import dto.ProductVariantDTO
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*


@Composable
fun ProductVariantCard(
    modifier: Modifier,
    product: ProductDTO,
    imageUrl: String,
    title: String,
    labels: List<LabelDTO>
) {
    var selectedPack by remember { mutableStateOf(4) } // Default to 12-pack
    val breakpoint = rememberBreakpoint()

    Column(
        modifier = Modifier
            .backgroundColor(Colors.DimGray)
            .width(100.percent)
            .maxWidth(500.px)
            .borderRadius(r = 20.px),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(20.px)
    ) {
        Box(
            modifier = Modifier
                .width(100.percent)
                .borderRadius(topRight = 15.px, topLeft = 15.px)
                .backgroundColor(rgba(0, 0, 0, 0.4))
                .maxWidth(500.px),
            contentAlignment = Alignment.TopStart
        ) {
            Image(
                src = imageUrl,
                modifier = Modifier
                    .borderRadius(topRight = 17.px, topLeft = 17.px)
                    .width(100.percent)
                    .maxWidth(500.px)
                    .maxHeight(283.px)
                    .objectFit(ObjectFit.ScaleDown),
                alt = title
            )
        }
        Column(
            modifier = Modifier
                .width(100.percent)
                .padding(10.px)
        ) {
            ProductBundleLabel(
                selectedPack = selectedPack,
                onPackSelected = { selectedPack = it },
                labels = labels
            )
            ProductDetails(selectedPack = selectedPack, product)


        }
    }
}

@Composable
fun ProductBundleLabel(selectedPack: Int, onPackSelected: (Int) -> Unit, labels: List<LabelDTO>) {
    var expanded by remember { mutableStateOf(false) }


    Column(modifier = Modifier.width(100.percent)) {
        Row(
            modifier = Modifier
                .width(100.percent)
                .maxWidth(500.px),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .width(100.percent)
                    .maxWidth(250.px),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.px) // if multiple labels
                ) {
                    labels.forEach { label ->
                        P(
                            attrs = Modifier
                                .fontFamily(FONT_FAMILY)
                                .color(Colors.White)
                                .padding(4.px)
                                .fontSize(15.px)
                                .backgroundColor(rgba(0, 0, 0, 0.4))
                                .toAttrs()
                        ) {
                            Text(formatLabel(label.name)) // ✅ "LIMITED_STOCK" → "Limited Stock"
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .width(100.percent)
                    .maxWidth(250.px),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Div(attrs = Modifier.classNames("dropdown").toAttrs()) {
                    Button(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .backgroundColor(rgba(0, 0, 0, 0.6))
                            .classNames("btn", "btn-secondary", "dropdown-toggle")
                            .onClick { expanded = !expanded }
                            .toAttrs()
                    ) {
                        Text("$selectedPack Bottles")
                    }

                    if (expanded) {
                        Div(
                            attrs = Modifier.classNames("dropdown-menu", "show", "dropdown-menu-end")
                                .styleModifier {
                                    fontFamily(FONT_FAMILY)
                                    property("max-width", "150px")
                                    property("min-width", "120px")
                                }
                                .toAttrs()
                        ) {
                            listOf(24, 12, 8, 4).forEach { count ->
                                A(
                                    attrs = Modifier
                                        .classNames("dropdown-item")
                                        .onClick {
                                            onPackSelected(count)
                                            expanded = false
                                        }
                                        .toAttrs()
                                ) {
                                    Text("$count Bottles")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetails(selectedPack: Int, product: ProductDTO) {
    val scope = rememberCoroutineScope()
    val isMobile = rememberBreakpoint() <= Breakpoint.MD

    var isSubscription by remember { mutableStateOf(false) }
    var variants by remember { mutableStateOf<List<ProductVariantDTO>>(emptyList()) }
    var selectedVariant by remember { mutableStateOf<ProductVariantDTO?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) } // Toast state

    val discountMap = mapOf(4 to 0.05, 12 to 0.08, 24 to 0.10)
    val discount = discountMap[selectedPack] ?: 0.0

    val basePrice = selectedVariant?.price ?: 0.0
    val discountedPrice = (basePrice * (1 - discount)).toInt()

    // Fetch variants
    LaunchedEffect(product.id) {
        try {
            println("📦 Fetching variants for product ID: ${product.id}")
            variants = JsProductFetcher().fetchProductVariants(product.id.toString())
            println("✅ Variants loaded: ${variants.size}")
        } catch (e: Exception) {
            println("❌ Error loading variants: ${e.message}")
        }
    }

    // Update selected variant based on pack & availability
    LaunchedEffect(selectedPack, variants) {
        println("🔄 Looking for variant with quantity $selectedPack...")
        selectedVariant = variants.firstOrNull { it.quantity == selectedPack && it.isAvailable }
        println("✅ Selected variant: $selectedVariant")
    }

    // Add to cart function
    val addToCart = fun() {
        val firebaseUid = window.localStorage.getItem("firebaseUserUID")
        val variantId = selectedVariant?.id

        if (firebaseUid == null || variantId == null) {
            toastMessage = "❌ Missing user ID or variant ID"
            return
        }

        val item = CartItemDTO(
            id = null,
            firebaseUid = firebaseUid,
            quantity = 1,
            productVariantId = variantId
        )

        scope.launch {
            try {
                val response = JsCartFetcher().addToCart(item)
                toastMessage = " Added to cart"
                println("Added to cart: $response")
            } catch (e: Exception) {
                toastMessage = "Failed to add to cart"
                println("Failed to add to cart: ${e.message}")
            }

            // Hide toast after 3 seconds
            delay(3000)
            toastMessage = null
        }
    }

    Column {
        // --- Always show product info ---
        P(
            attrs = Modifier
                .fontSize(24.px)
                .fontWeight(FontWeight.Bold)
                .fontFamily(FONT_FAMILY)
                .color(Colors.White)
                .toAttrs()
        ) {
            Text(product.name)
        }

        P(
            attrs = Modifier.fontFamily(FONT_FAMILY).color(Colors.White).toAttrs()
        ) {
            Text(product.description ?: "")
        }

        P(
            attrs = Modifier.fontFamily(FONT_FAMILY).color(Colors.White).toAttrs()
        ) {
            Text("Size: ${selectedVariant?.size ?: "N/A"}")
        }

        P(
            attrs = Modifier.fontFamily(FONT_FAMILY).color(Colors.White).toAttrs()
        ) {
            Text("Price: PHP ${if (isSubscription) discountedPrice else basePrice.toInt()}")
        }

        val variantAvailable = selectedVariant?.isAvailable ?: false

        if (!variantAvailable) {
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .color(Colors.White)
                    .fontWeight(FontWeight.Bold)
                    .toAttrs()
            ) {
                Text("Currently Unavailable")
            }
        }

        // --- Add to Cart Button ---
        Button(
            attrs = Modifier
                .disabled(!variantAvailable)
                .boxShadow(
                    blurRadius = 2.px,
                    spreadRadius = 0.px,
                    color = Colors.Grey,
                    offsetX = 0.px,
                    offsetY = 1.px
                )
                .color(Colors.White)
                .backgroundColor(
                    if (variantAvailable) rgba(0, 0, 0, 0.4)
                    else rgba(128, 128, 128, 0.4)
                )
                .fontWeight(FontWeight.Medium)
                .fontSize(12.px)
                .border(style = LineStyle.None)
                .padding(5.px)
                .fontFamily(FONT_FAMILY)
                .borderRadius(r = 5.px)
                .onClick { if (variantAvailable) addToCart() }
                .toAttrs()
        ) {
            Text("ADD TO CART")
        }

        // --- Toast ---
        ToastMessage(toastMessage)
    }
}

