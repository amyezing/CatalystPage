package catalystpage.com.admin

import admin.dto.AdminPanel
import androidx.compose.runtime.*
import catalystpage.com.admin.adashboards.*
import catalystpage.com.admin.model.AdminScheduleManager
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun AdminDashboard() {
    var selectedPanel by remember { mutableStateOf(AdminPanel.ORDERS) }
    val toastMessage = remember { mutableStateOf<String?>(null) }
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fontFamily(FONT_FAMILY),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 👇 push content to the right when sidebar is visible
                .padding(
                    left = if (isMobile) 0.px else 150.px,
                    top = 16.px,
                    right = 16.px,
                    bottom = 16.px
                )
        ) {
            when (selectedPanel) {
                AdminPanel.ORDERS -> OrderSummary()
                AdminPanel.PAYMENTS -> PaymentSummary()
                AdminPanel.SHIPPING -> ShippingSummary(toastMessage = toastMessage)
                AdminPanel.SCHEDULE_MANAGER -> AdminScheduleManager()
                AdminPanel.ECO_RECYCLING -> RecyclingSection()
                AdminPanel.STOCK -> StockReport()
                AdminPanel.USERS -> UserList()
                AdminPanel.PRODUCTS -> ProductManager()
                AdminPanel.AUDIT -> AuditLogs()
                AdminPanel.ECO_POINTS -> EcoPoints()
            }
        }
    }

    // ✅ Nav stays fixed on top of everything
    AdminNav(selected = selectedPanel, onSelect = { selectedPanel = it })
}