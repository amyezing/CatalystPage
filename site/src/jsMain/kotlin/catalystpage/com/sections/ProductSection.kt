package catalystpage.com.sections

import androidx.compose.runtime.*
import catalystpage.com.components.ProductCard
import catalystpage.com.components.SectionTitle
import catalystpage.com.database.JsProductFetcher
import catalystpage.com.model.Section
import catalystpage.com.styles.PortfolioArrowIconStyle
import catalystpage.com.util.Constants.SECTION_WIDTH
import catalystpage.com.wrapper.ProductUiState
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaArrowLeft
import com.varabyte.kobweb.silk.components.icons.fa.FaArrowRight
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.document
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text


@Composable
fun ProductSection() {
    Box(
        modifier = Modifier
            .id(Section.Products.id)
            .maxWidth(SECTION_WIDTH.px)
            .padding(topBottom = 100.px),
        contentAlignment = Alignment.Center
    ) {
        ProductContent()
    }

}

@Composable
fun ProductContent() {
    val breakpoint = rememberBreakpoint()
    Column(
        modifier = Modifier
            .fillMaxWidth(
                if (breakpoint >= Breakpoint.MD) 100.percent
                else 90.percent
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(
            modifier = Modifier
                .fillMaxWidth()
                .margin(bottom = 25.px),
            section = Section.Products
        )
        ProductsCards(breakpoint = breakpoint)
        ProductsNavigation()
    }

}


@Composable
fun ProductsCards(breakpoint: Breakpoint) {
    val uiState = remember { mutableStateOf<ProductUiState>(ProductUiState.Loading) }

    LaunchedEffect(Unit) {
        try {
            val products = JsProductFetcher().fetchProducts()
            uiState.value = ProductUiState.Success(products)
        } catch (e: Exception) {
            uiState.value = ProductUiState.Error("Failed to fetch products: ${e.message}")
        }
    }

    when (val state = uiState.value) {
        is ProductUiState.Loading -> {
            // Optional: Replace with a loading spinner if you want
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Loading products...")
            }
        }

        is ProductUiState.Error -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(state.message)
            }
        }

        is ProductUiState.Success -> {
            if (state.products.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .id("scrollableContainer")
                        .fillMaxWidth()
                        .margin(bottom = 25.px)
                        .maxWidth(
                            when {
                                breakpoint > Breakpoint.MD -> 950.px
                                breakpoint > Breakpoint.SM -> 625.px
                                else -> 300.px
                            }
                        )
                        .overflow(Overflow.Hidden)
                        .scrollBehavior(ScrollBehavior.Smooth)
                ) {
                    state.products.forEachIndexed { index, product ->
                        ProductCard(
                            modifier = Modifier.margin(
                                right = if (index != state.products.lastIndex) 25.px else 0.px
                            ),
                            product = product,
                            link = "#"
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No products available.")
                }
            }
        }
    }
}

@Composable
fun ProductsNavigation() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        FaArrowLeft(
            modifier = PortfolioArrowIconStyle.toModifier()
                .margin(right = 40.px)
                .cursor(Cursor.Pointer)
                .onClick {
                    document.getElementById("scrollableContainer")
                        ?.scrollBy(x = (-325.0), y = 0.0)
                },
            size = IconSize.LG
        )
        FaArrowRight(
            modifier = PortfolioArrowIconStyle.toModifier()
                .cursor(Cursor.Pointer)
                .onClick {
                    document.getElementById("scrollableContainer")
                        ?.scrollBy(x = 325.0, y = 0.0)
                },
            size = IconSize.LG
        )
    }
}






