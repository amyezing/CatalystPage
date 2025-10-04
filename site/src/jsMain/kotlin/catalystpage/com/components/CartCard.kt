package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.wrapper.CartViewModel
import catalystpage.com.wrapper.getQuantityAsState
import com.varabyte.kobweb.compose.css.AlignItems
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.JustifyContent
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
import com.varabyte.kobweb.silk.components.icons.fa.FaMinus
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.CartItemUI
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
@Composable
fun CartCard(
    item: CartItemUI,
    cartViewModel: CartViewModel,
    updateCartCount: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val breakpoint = rememberBreakpoint()
    var zoomedImage by remember { mutableStateOf<String?>(null) }
    var showBox by remember { mutableStateOf(false) }
    var boxPositionX by remember { mutableStateOf(0.0) }
    var boxPositionY by remember { mutableStateOf(0.0) }
    var offsetX by remember { mutableStateOf(0.0) }
    var offsetY by remember { mutableStateOf(0.0) }
    var isDragging by remember { mutableStateOf(false) }
    var startX by remember { mutableStateOf(0.0) }
    var startY by remember { mutableStateOf(0.0) }
    val isProduct = item.productVariantId != null
    val currentQuantity by cartViewModel.getQuantityAsState(item.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(
                when {
                    breakpoint <= Breakpoint.SM -> 350.px
                    breakpoint <= Breakpoint.MD -> 500.px
                    else -> 800.px
                }
            )
            .margin(20.px)
    ) {
        // ✅ Main Grid (Product / Quantity / Total)
        SimpleGrid(
            numColumns = numColumns(base = 1, md = 3),
            modifier = Modifier
                .fillMaxWidth()
                .backgroundColor(rgba(255, 255, 255, 0.5))
                .borderRadius(10.px)
                .gap(16.px)
                .padding(10.px)
        ) {
            // ✅ PRODUCT IMAGE + NAME
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.onClick { event ->
                        event.stopPropagation()
                        showBox = true
                        zoomedImage = item.imageUrl
                        boxPositionX = event.clientX.toDouble() + 20
                        boxPositionY = event.clientY.toDouble() - 50
                    }
                ) {
                    Image(
                        src = item.imageUrl,
                        alt = "Image",
                        modifier = Modifier.size(80.px)
                            .borderRadius(10.px)
                            .cursor(Cursor.ZoomIn)
                    )
                }

                Column(
                    Modifier.margin(left = 10.px)
                        .width(150.px)
                        .fontFamily(FONT_FAMILY)
                ) {
                    Text("${item.title} Pack: ${item.packSize}")
                    RemoveItem(item.id) {
                        scope.launch {
                            cartViewModel.removeItem(it)
                            updateCartCount()
                        }
                    }
                }
            }

            // ✅ QUANTITY SELECTOR
            CartQuantity(
                itemCount = currentQuantity,
                onItemCountChange = { newQty ->
                    scope.launch { cartViewModel.updateQuantity(item.id, newQty) }
                },
                updateCartCount = updateCartCount
            )

            // ✅ TOTAL PRICE
            P(attrs = Modifier.fontFamily(FONT_FAMILY)
                .fontWeight(FontWeight.Bold)
                .toAttrs()
            ) {
                Text("₱${item.packPrice * item.quantity}.00")
            }
        }

        // ✅ IMAGE ZOOM POPUP
        if (showBox && zoomedImage != null && breakpoint > Breakpoint.SM) {
            val zoomWidth = if (isProduct) 450.px else 1000.px
            val zoomHeight = if (isProduct) 800.px else 600.px
            val dragLimitX = if (isProduct) -450.0 else -500.0
            val dragLimitY = if (isProduct) -800.0 else -300.0

            Box(
                Modifier
                    .position(Position.Absolute)
                    .top(boxPositionY.px)
                    .left(boxPositionX.px)
                    .border(1.px, color = Colors.Black)
                    .size(500.px, 300.px)
                    .zIndex(1.0)
                    .onClick { it.stopPropagation() }
                    .onMouseOut { showBox = false }
            ) {
                Box(
                    Modifier
                        .cursor(Cursor.Move)
                        .size(zoomWidth, zoomHeight)
                        .styleModifier {
                            property("transform", "translate(${offsetX}px, ${offsetY}px)")
                            property("transition", "transform 0.1s ease-out")
                        }
                        .onMouseDown {
                            isDragging = true
                            startX = it.clientX.toDouble()
                            startY = it.clientY.toDouble()
                        }
                        .onMouseMove {
                            if (isDragging) {
                                offsetX = (offsetX + it.clientX.toDouble() - startX)
                                    .coerceIn(dragLimitX, 0.0)
                                offsetY = (offsetY + it.clientY.toDouble() - startY)
                                    .coerceIn(dragLimitY, 0.0)
                                startX = it.clientX.toDouble()
                                startY = it.clientY.toDouble()
                            }
                        }
                        .onMouseUp { isDragging = false }
                ) {
                    Image(
                        src = zoomedImage!!,
                        alt = "Zoomed",
                        modifier = Modifier.size(zoomWidth, zoomHeight)
                    )
                }
            }
        }
    }
}



@Composable
fun CartQuantity(
    itemCount: Int,
    onItemCountChange: (Int) -> Unit,
    updateCartCount: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .border(1.px, color = Colors.Gray)
            .padding(4.px)
            .height(32.px) // Ensures consistent height with the icons
            .alignItems(AlignItems.Center), // Center everything vertically
    ) {
        FaMinus(
            modifier = Modifier
                .cursor(Cursor.Pointer)
                .onClick {
                    if (itemCount > 1) {
                        scope.launch {
                            onItemCountChange(itemCount - 1)
                            updateCartCount()
                        }

                    }
                },
            size = IconSize.SM
        )

        Box(
            modifier = Modifier
                .margin(leftRight = 10.px)
                .height(16.px)
                .display(DisplayStyle.Flex)
                .alignItems(AlignItems.Center)
                .justifyContent(JustifyContent.Center)
        ) {
            Text(itemCount.toString())
        }

        FaPlus(
            modifier = Modifier
                .cursor(Cursor.Pointer)
                .onClick {
                    scope.launch {
                        onItemCountChange(itemCount + 1)
                        updateCartCount()
                    }

                },
            size = IconSize.SM
        )
    }
}

@Composable
fun RemoveItem(cartItemId: Int, onRemove: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    P( attrs = Modifier
        .padding(5.px)
        .borderRadius(r = 10.px)
        .fontSize(10.px)
        .fontFamily("Arial")
        .backgroundColor(Colors.PaleTurquoise)
        .color(Colors.Black)
        .cursor(Cursor.Pointer)
        .onClick {
            scope.launch {
                onRemove(cartItemId)

            }
        }
        .toAttrs()
    ) {
        Text("DELETE")
    }
}




