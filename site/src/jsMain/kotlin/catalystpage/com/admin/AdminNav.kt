package catalystpage.com.admin

import admin.dto.AdminPanel
import androidx.compose.runtime.Composable
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun AdminNav(
    selected: AdminPanel,
    onSelect: (AdminPanel) -> Unit
) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    if (isMobile) {
        // 🔹 Bottom navigation for mobile
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
            AdminPanel.entries.forEach { panel ->
                AdminIcon(
                    panel = panel,
                    isSelected = panel == selected,
                    onClick = { onSelect(panel) }
                )
            }
        }
    } else {
        // 🔹 Sidebar for desktop
        Column(
            modifier = Modifier
                .width(150.px)
                .fillMaxHeight()
                .position(Position.Fixed)
                .top(0.px)
                .left(0.px)
                .zIndex(100)
                .backgroundColor(Colors.White)
                .borderRadius(topRight = 10.px, bottomRight = 10.px)
                .boxShadow(
                    blurRadius = 10.px,
                    color = Colors.Gainsboro,
                    offsetX = 1.px
                )
                .overflow(Overflow.Hidden)
                .pointerEvents(PointerEvents.Auto),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                AdminPanel.entries.forEach { panel ->
                    AdminIcon(
                        panel = panel,
                        isSelected = panel == selected,
                        onClick = { onSelect(panel) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminIcon(panel: AdminPanel, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Colors.LightBlue else Colors.White
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    val modifier = if (isMobile) {
        Modifier
            .size(44.px)
            .backgroundColor(backgroundColor)
            .borderRadius(50.percent)
            .cursor(Cursor.Pointer)
            .onClick { onClick() }
            .display(DisplayStyle.Flex)
            .alignItems(AlignItems.Center)
            .justifyContent(JustifyContent.Center)
    } else {
        Modifier
            .fillMaxWidth()
            .margin(topBottom = 10.px, leftRight = 10.px)
            .backgroundColor(backgroundColor)
            .borderRadius(5.px)
            .padding(topBottom = 10.px, leftRight = 5.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() }
    }

    Box(modifier = modifier) {
        if (isMobile) {
            AdminIconGraphic(panel)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.px)
            ) {
                AdminIconGraphic(panel)
                P(
                    attrs = Modifier
                        .margin(top = 2.px)
                        .fontSize(14.px)
                        .fontFamily(FONT_FAMILY)
                        .toAttrs()
                ) {
                    Text(panel.title)
                }
            }
        }
    }
}

@Composable
fun AdminIconGraphic(panel: AdminPanel) {
    when (panel) {
        AdminPanel.ORDERS -> FaClipboardList(size = IconSize.LG)
        AdminPanel.PAYMENTS -> FaMoneyBillWave(size = IconSize.LG)
        AdminPanel.ECO_RECYCLING -> FaRecycle(size = IconSize.LG)
        AdminPanel.SCHEDULE_MANAGER -> FaCalendar(size = IconSize.LG)
        AdminPanel.STOCK -> FaWarehouse(size = IconSize.LG)
        AdminPanel.USERS -> FaUsers(size = IconSize.LG)
        AdminPanel.PRODUCTS -> FaBoxOpen(size = IconSize.LG)
        AdminPanel.AUDIT -> FaCalculator(size = IconSize.LG)
        AdminPanel.SHIPPING -> FaTruck(size = IconSize.LG)
        AdminPanel.ECO_POINTS -> FaBarcode(size = IconSize.LG)

    }
}