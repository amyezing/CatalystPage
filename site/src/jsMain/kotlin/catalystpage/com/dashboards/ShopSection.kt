package catalystpage.com.dashboards

import androidx.compose.runtime.*

import catalystpage.com.components.ProductVariantCard
import catalystpage.com.database.JsProductFetcher
import catalystpage.com.model.DashboardItem
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint

import dto.ProductDTO
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

@Composable
fun ShopSection(updateCartCount: () -> Unit) {
    Box(
        modifier = Modifier
            .id(DashboardItem.Shop.id)
            .fillMaxWidth()
            .maxWidth(1920.px) // ✅ allow full HD width
            .margin(topBottom = 20.px), // vertical spacing only
        contentAlignment = Alignment.TopCenter
    ) {
        ShopContent(updateCartCount)
    }
}

@Composable
fun ShopContent(updateCartCount: () -> Unit) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD
    var products by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }

    // ✅ Fetch products
    LaunchedEffect(Unit) {
        try {
            products = JsProductFetcher().fetchProducts()
        } catch (e: Exception) {
            console.error("❌ Failed to load products: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(1200.px)
            .margin(bottom = 25.px)
            .then(
                if (!isMobile) {
                    Modifier.styleModifier {
                        property("margin-left", "auto")
                        property("margin-right", "auto")
                    }
                } else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleGrid(
            numColumns = numColumns(base = 1, md = 2), // ✅ 1 col mobile, 2 col desktop
            modifier = Modifier
                .fillMaxWidth()
                .gap(20.px) // ✅ smaller gap on mobile
                .then(
                    if (isMobile) {
                        Modifier.styleModifier {
                            property("justify-items", "center") // ✅ center cards
                        }
                    } else {
                        Modifier.styleModifier {
                            property("justify-items", "stretch") // ✅ balance desktop
                        }
                    }
                )
        ) {
            products.forEach { product ->
                ProductVariantCard(
                    product = product,
                    imageUrl = product.imageUrl
                        ?: "https://storage.googleapis.com/catalyst-cloud-storage/20.png",
                    title = product.name,
                    labels = product.labels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxWidth(if (isMobile) 350.px else 400.px) // ✅ smaller cap on mobile
                )
            }
        }
    }
}
