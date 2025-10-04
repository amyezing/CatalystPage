package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.database.JsCartFetcher
import catalystpage.com.database.logOutUser
import catalystpage.com.model.DashboardItem
import catalystpage.com.styles.SocialLinkStyle
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.PointerEvents
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text


@Composable
fun Dashflow(
    selectedItem: DashboardItem,
    onItemSelected: (DashboardItem) -> Unit,
    firebaseUid: String,
    cartCount: Int,
    updateCartCount: () -> Unit
) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    LaunchedEffect(firebaseUid) { updateCartCount() }


    if (isMobile) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.px)
                .position(Position.Fixed)
                .bottom(0.px)
                .left(0.px)
                .zIndex(100)
                .backgroundColor(Colors.White)
                .overflow(Overflow.Hidden)
                .pointerEvents(PointerEvents.Auto),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardItem.entries.forEach { item ->
                FaIcon(
                    item = item,
                    isSelected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    cartCount = if (item == DashboardItem.Cart) cartCount else null
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .width(150.px)
                .fillMaxHeight()
                .position(Position.Fixed)
                .top(0.px)
                .left(0.px)
                .zIndex(100)
                .backgroundColor(rgba(255, 255, 255, 0.6))
                .borderRadius(topRight = 10.px, bottomRight = 10.px)
                .overflow(Overflow.Hidden)
                .pointerEvents(PointerEvents.Auto),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            DashLinks(
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                cartCount = cartCount,
                updateCartCount = updateCartCount
            )
        }
    }
}

@Composable
private fun DashLinks(
    selectedItem: DashboardItem,
    onItemSelected: (DashboardItem) -> Unit,
    cartCount: Int,
    updateCartCount: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        DashboardItem.entries.forEach { item ->
            FaIcon(
                item = item,
                isSelected = selectedItem == item,
                onClick = { onItemSelected(item) },
                cartCount = if (item == DashboardItem.Cart) cartCount else null
            )
        }
        Logout()
    }
}


@Composable
private fun FaIcon(
    item: DashboardItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    cartCount: Int? = null
) {
    val backgroundColor = if (isSelected) Colors.PaleTurquoise else rgba(255, 255, 255, 0.4)
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    val modifier = if (isMobile) {
        Modifier
            .size(44.px)
            .backgroundColor(backgroundColor)
            .borderRadius(topLeft = 30.px, topRight = 30.px, bottomRight = 30.px, bottomLeft = 30.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() }
            .display(DisplayStyle.Flex)
            .alignItems(AlignItems.Center)
            .justifyContent(JustifyContent.Center)
            .position(Position.Relative) // important for badge alignment
    } else {
        SocialLinkStyle.toModifier()
            .fillMaxWidth()
            .margin(topBottom = 10.px, leftRight = 10.px)
            .backgroundColor(backgroundColor)
            .borderRadius(r = 5.px)
            .padding(topBottom = 10.px, leftRight = 3.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() }
            .position(Position.Relative) // important for badge alignment
    }

    Box(modifier = modifier) {
        if (isMobile) {
            when (item) {
                DashboardItem.Profile -> FaHouseUser(size = IconSize.LG)
                DashboardItem.Shop -> FaShop(size = IconSize.LG)
                DashboardItem.Cart -> FaCartShopping(size = IconSize.LG)
                DashboardItem.Orders -> FaClipboardList(size = IconSize.LG)
                DashboardItem.Settings -> FaGear(size = IconSize.LG)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.px)
            ) {
                when (item) {
                    DashboardItem.Profile -> FaHouseUser(size = IconSize.LG)
                    DashboardItem.Shop -> FaShop(size = IconSize.LG)
                    DashboardItem.Cart -> FaCartShopping(size = IconSize.LG)
                    DashboardItem.Orders -> FaClipboardList(size = IconSize.LG)
                    DashboardItem.Settings -> FaGear(size = IconSize.LG)
                }
                P(
                    attrs = Modifier
                        .margin(top = 2.px)
                        .fontSize(14.px)
                        .fontFamily(FONT_FAMILY)
                        .toAttrs()
                ) {
                    Text(item.title)
                }
            }
        }

        // Badge
        if (cartCount != null && cartCount > 0) {
            Box(
                modifier = Modifier
                    .position(Position.Absolute)
                    .top((-2).px) // tweak these to pin closer to corner
                    .right(10.px)
                    .margin(right = 20.px)
                    .zIndex(200)
            ) {
                P(
                    attrs = Modifier
                        .backgroundColor(Colors.PaleTurquoise)
                        .padding(topBottom = 5.px, leftRight = 10.px)
                        .borderRadius(r = 15.px)
                        .color(Colors.Black)
                        .fontSize(10.px)
                        .fontWeight(FontWeight.Bold)
                        .fontFamily(FONT_FAMILY)
                        .toAttrs()
                ) {
                    Text(cartCount.toString())
                }
            }
        }
    }
}

@Composable
fun Logout() {
    val scope = rememberCoroutineScope()
    val pageContext = rememberPageContext()
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    val modifier = if (isMobile) {
        Modifier
            .size(44.px)
            .backgroundColor(Colors.White)
            .borderRadius(50.percent)
            .cursor(Cursor.Pointer)
            .onClick {
                scope.launch {
                    logOutUser()
                    pageContext.router.navigateTo("/signIn")
                }
            }
            .display(DisplayStyle.Flex)
            .alignItems(AlignItems.Center)
            .justifyContent(JustifyContent.Center)
    } else {
        SocialLinkStyle.toModifier()
            .fillMaxWidth()
            .margin(topBottom = 10.px, leftRight = 10.px)
            .backgroundColor(rgba(255, 255, 255, 0.4))
            .borderRadius(r = 5.px)
            .padding(topBottom = 10.px)
            .cursor(Cursor.Pointer)
            .onClick {
                scope.launch {
                    logOutUser()
                    pageContext.router.navigateTo("/signIn")
                }
            }
    }

    Box(modifier = modifier) {
        if (isMobile) {
            FaDoorOpen(size = IconSize.LG)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.px)
            ) {
                FaDoorOpen(size = IconSize.LG)
                P(
                    attrs = Modifier
                        .margin(top = 2.px)
                        .fontSize(14.px)
                        .fontFamily(FONT_FAMILY)
                        .toAttrs()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

