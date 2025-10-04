package catalystpage.com.sections

import androidx.compose.runtime.*
import catalystpage.com.admin.AdminDashboard
import catalystpage.com.components.Dashflow
import catalystpage.com.components.OrderDetailsCard
import catalystpage.com.dashboards.*
import catalystpage.com.database.JsCartFetcher
import catalystpage.com.database.JsUserFetcher
import catalystpage.com.model.DashboardItem
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.Role
import dto.UserDTO
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text

@Composable
fun DashboardSection(onMenuClicked: () -> Unit) {
    var selectedItem by remember { mutableStateOf(DashboardItem.Profile) }
    var user by remember { mutableStateOf<UserDTO?>(null) }
    var showCheckout by remember { mutableStateOf(false) }
    var currentOrderId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var cartCount by remember { mutableStateOf(0) }

    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD
    val isTablet = breakpoint == Breakpoint.LG
    val isDesktop = breakpoint >= Breakpoint.XL

    val sidebarWidth = when {
        isDesktop -> 180.px
        isTablet -> 140.px
        else -> 0.px
    }

    fun updateCartCount() {
        val uid = user?.firebaseUid ?: return
        scope.launch {
            try {
                val count = JsCartFetcher().getCartCount(uid)
                cartCount = count
            } catch (e: Exception) {
                console.error("Failed to fetch cart count: $e")
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val dto = JsUserFetcher().fetchCurrentUser()
                val resolvedRole = Role.fromStringOrNull(dto.roleRaw)
                user = dto.copy(role = resolvedRole)
                updateCartCount()
            } catch (e: Exception) {
                console.error("Error fetching user:", e)
            }
        }
    }

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading user data…")
        }
        return
    }

    if (showCheckout && currentOrderId != null) {
        if (selectedItem == DashboardItem.Cart) {
            CheckoutPage(
                orderId = currentOrderId!!,
                firebaseUid = user!!.firebaseUid,
                onBackToCart = { showCheckout = false }
            )
        } else if (selectedItem == DashboardItem.Orders) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .backgroundColor(Colors.PaleTurquoise),
                contentAlignment = Alignment.Center) {
                OrderDetailsCard(
                    orderId = currentOrderId!!,
                    firebaseUid = user!!.firebaseUid,
                    onBack = { showCheckout = false }
                )
            }

        }
        return
    }

    Box(
        modifier = Modifier
            .backgroundColor(Colors.PaleTurquoise)
            .fillMaxSize()
            .overflow(Overflow.Hidden)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1)
        ) {
            if (user?.role == Role.USER && !isMobile) {
                Box(
                    modifier = Modifier
                        .height(100.percent)
                        .width(sidebarWidth)
                        .zIndex(3)
                ) {
                    Dashflow(
                        selectedItem = selectedItem,
                        onItemSelected = { selectedItem = it },
                        firebaseUid = user!!.firebaseUid,
                        cartCount = cartCount,
                        updateCartCount = ::updateCartCount
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .flexGrow(1)
                    .zIndex(1)
            ) {
                when (user?.role) {
                    Role.ADMIN -> AdminDashboard()
                    Role.USER -> {
                        when (selectedItem) {
                            DashboardItem.Profile -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .position(Position.Relative)
                                        .zIndex(1)
                                        .padding(
                                            left = if (isMobile) 12.px else if (isTablet) 20.px else 28.px,
                                            right = 0.px,
                                            top = if (isMobile) 12.px else if (isTablet) 20.px else 28.px,
                                            bottom = if (isMobile) 12.px else if (isTablet) 20.px else 28.px,
                                        )
                                ) {val zoneId = user?.zoneId ?: 1
                                    ProfileSection(zoneId = zoneId) // already contains the status card
                                }
                            }
                            DashboardItem.Shop -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            top = if (isMobile) 12.px else if (isTablet) 20.px else 28.px,
                                            bottom = if (isMobile) 60.px else 80.px
                                        ),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .maxWidth(1920.px) // ✅ allow full HD width
                                            .padding(leftRight = 16.px)
                                    ) {
                                        ShopSection(updateCartCount = ::updateCartCount)
                                    }
                                }
                            }
                            DashboardItem.Cart -> CartSection(
                                updateCartCount = ::updateCartCount,
                                firebaseUid = user!!.firebaseUid,
                                onCheckoutSuccess = { orderId ->
                                    currentOrderId = orderId
                                    showCheckout = true
                                }
                            )
                            DashboardItem.Orders -> MyOrdersSection(
                                firebaseUid = user!!.firebaseUid,
                                onViewOrder = { orderId ->
                                    currentOrderId = orderId
                                    showCheckout = true
                                }
                            )
                            DashboardItem.Settings -> SettingsSection(firebaseUid = user!!.firebaseUid)
                        }
                    }
                    else -> Text("Unauthorized")
                }
            }
        }

        DashboardContent(onMenuClicked = onMenuClicked)

        if (user?.role == Role.USER && isMobile) {
            Dashflow(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                firebaseUid = user!!.firebaseUid,
                cartCount = cartCount,
                updateCartCount = ::updateCartCount
            )
        }
    }
}


@Composable
fun DashboardContent(onMenuClicked: () -> Unit) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD
    val isTablet = breakpoint == Breakpoint.LG
    val isDesktop = breakpoint >= Breakpoint.XL

    val contentWidth = when {
        isMobile -> 95.percent
        isTablet -> 92.percent
        else -> 80.percent
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleGrid(
            modifier = Modifier.fillMaxWidth(contentWidth),
            numColumns = numColumns(
                base = 1,  // mobile
                md = 2,    // tablet
                lg = 3     // desktop
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // toolbar/header area
            }
        }
    }
}