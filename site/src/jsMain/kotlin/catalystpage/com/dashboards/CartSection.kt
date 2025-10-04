package catalystpage.com.dashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.isShippingReady
import catalystpage.com.components.CartCard
import catalystpage.com.components.ToastMessage
import catalystpage.com.database.*
import catalystpage.com.styles.ButtonStyles
import catalystpage.com.styles.ClearTextStyle
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Constants.ROBOTO_SERIF
import catalystpage.com.wrapper.CartViewModel
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.AlignItems
import com.varabyte.kobweb.compose.css.JustifyContent
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.CartItemDTO
import dto.ShippingDetailsDTO
import dto.ShippingStatus
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlin.js.Date


@Composable
fun CartSection(
    firebaseUid: String,
    onCheckoutSuccess: (Int) -> Unit,
    updateCartCount: () -> Unit
) {
    val breakpoint = rememberBreakpoint()
    val viewModel = remember { CartViewModel() }

    // Center the page container so everything lines up
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .maxWidth(
                    when {
                        breakpoint <= Breakpoint.SM -> 100.percent   // phones
                        breakpoint <= Breakpoint.MD -> 95.percent    // tablets
                        else -> 1000.px                              // desktops
                    }
                )
                .margin(topBottom = 32.px, leftRight = 16.px)
                .padding(20.px)
        ) {
            CartPage(
                firebaseUid = firebaseUid,
                viewModel = viewModel,
                onCheckoutSuccess = onCheckoutSuccess,
                updateCartCount = updateCartCount
            )
        }
    }
}

@Composable
fun CartPage(
    firebaseUid: String,
    viewModel: CartViewModel,
    onCheckoutSuccess: (Int) -> Unit,
    updateCartCount: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 5
    val breakpoint = rememberBreakpoint()
    val cartItems = viewModel.cartItems
    var showCheckoutPage by remember { mutableStateOf(false) }
    var currentOrderId by remember { mutableStateOf<Int?>(null) }

    var street by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf<DeliveryOption?>(null) }
    var selectedCourier by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }
    val toastMessage = remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    val isMobileOrTablet = breakpoint <= Breakpoint.MD

    val address = listOf(street, barangay, city, zipCode)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    LaunchedEffect(firebaseUid) {
        val user = JsUserFetcher().fetchCurrentUser()
        phoneNumber = user.phone ?: ""
        val pendingOrderId = CheckoutService.getPendingOrder(firebaseUid)
        if (pendingOrderId != null) {
            currentOrderId = pendingOrderId
            showCheckoutPage = true
        } else {
            viewModel.loadCartItems(firebaseUid)
        }
    }

    if (showCheckoutPage && currentOrderId != null) {
        CheckoutPage(
            orderId = currentOrderId!!,
            firebaseUid = firebaseUid,
            onBackToCart = { showCheckoutPage = false }
        )
        return
    }

    val mergedItems = cartItems
        .groupBy { Pair(it.title, it.packSize) }
        .map { (_, group) ->
            val first = group.first()
            first.copy(quantity = group.sumOf { it.quantity })
        }

    val totalPages = (mergedItems.size + itemsPerPage - 1) / itemsPerPage
    val itemsForPage = mergedItems.drop(currentPage * itemsPerPage).take(itemsPerPage)
    val totalPrice = mergedItems.sumOf { it.packPrice.toDouble() * it.quantity }

    val scope = rememberCoroutineScope()

    // ✅ Center whole page content
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .maxWidth(
                    when {
                        isMobileOrTablet -> 100.percent
                        else -> 1000.px
                    }
                )
                .gap(20.px)
                .margin(
                    topBottom = 20.px,
                    leftRight = if (isMobileOrTablet) 12.px else 0.px
                )
        ) {
            CartHeader(
                onClearClick = {
                    scope.launch {
                        viewModel.clearCart(firebaseUid)
                        updateCartCount()
                    }
                }
            )

            // CART ITEMS
            itemsForPage.forEach { item ->
                CartCard(
                    item = item,
                    cartViewModel = viewModel,
                    updateCartCount = { updateCartCount() }
                )
            }

            // PAGINATION
            if (totalPages > 1) {
                PaginationControls(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPrevious = { if (currentPage > 0) currentPage-- },
                    onNext = { if (currentPage < totalPages - 1) currentPage++ }
                )
            }

            // DELIVERY OPTIONS
            val isReadyToCheckout by remember {
                derivedStateOf {
                    when (selectedOption) {
                        DeliveryOption.PICKUP -> pickupDate.isNotBlank() && pickupTime.isNotBlank()
                        DeliveryOption.COURIER -> street.isNotBlank() && barangay.isNotBlank() &&
                                city.isNotBlank() && zipCode.isNotBlank() && selectedCourier.isNotBlank()
                        DeliveryOption.SCHEDULED -> street.isNotBlank() && barangay.isNotBlank() &&
                                city.isNotBlank() && zipCode.isNotBlank()
                        else -> false
                    }
                }
            }

            DeliveryOptions(
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it }
            )

            if (selectedOption == DeliveryOption.COURIER) {
                SelectCourierDropdown(
                    selectedCourier = selectedCourier,
                    onCourierSelected = { selectedCourier = it }
                )
            }

            if (selectedOption == DeliveryOption.COURIER || selectedOption == DeliveryOption.SCHEDULED) {
                SimpleGrid(
                    numColumns = numColumns(base = 1, md = 3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxWidth(if (isMobileOrTablet) 100.percent else 900.px)
                        .backgroundColor(rgba(255, 255, 255, 0.4))
                        .borderRadius(10.px)
                        .margin(top = 10.px)
                        .gap(16.px)
                        .padding(10.px)
                ) {
                    TextInputField("Block, Lot, Street", street, onValueChange = { street = it })
                    TextInputField("Village, Barangay, Bldg.#", barangay, onValueChange = { barangay = it })
                    CityDropdown(selectedCity = city, onCityChange = { city = it })
                    TextInputField("ZIP Code", zipCode, onValueChange = { zipCode = it })
                    TextInputField("Phone Number", phoneNumber, onValueChange = { phoneNumber = it })
                    SubmitShippingButton(
                        scope = scope,
                        selectedOption = selectedOption,
                        currentOrderId = currentOrderId,
                        setCurrentOrderId = { currentOrderId = it },
                        firebaseUid = firebaseUid,
                        street = street,
                        barangay = barangay,
                        city = city,
                        zipCode = zipCode,
                        selectedCourier = selectedCourier,
                        phoneNumber = phoneNumber,
                        userShipping = userShipping,
                        toastMessage = toastMessage
                    )

                    ToastMessage(message = toastMessage.value)
                }
            }

            val setCurrentOrderId: (Int) -> Unit = { currentOrderId = it }
            if (selectedOption == DeliveryOption.PICKUP) {
                PickUpSection(
                    initialPhoneNumber = phoneNumber,
                    currentOrderId = currentOrderId,
                    setCurrentOrderId = setCurrentOrderId,
                    firebaseUid = firebaseUid
                )
            }

            // CHECKOUT
            if (currentOrderId != null) {
                CheckoutSection(
                    orderId = currentOrderId!!,
                    firebaseUid = firebaseUid,
                    address = address,
                    totalPrice = totalPrice,
                    cartItem = cartItems.map {
                        CartItemDTO(
                            firebaseUid = firebaseUid,
                            quantity = it.quantity,
                            productVariantId = it.productVariantId,
                            price = it.packPrice.toDouble(),
                            productName = if (it.isProduct) it.title else null,
                        )
                    },
                    onCheckoutSuccess = { newOrderId ->
                        currentOrderId = newOrderId
                        showCheckoutPage = true
                    },
                    isReadyToCheckout = isReadyToCheckout
                )
            }
        }
    }
}


@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val breakpoint = rememberBreakpoint()
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(
            if (breakpoint <= Breakpoint.MD) 80.percent else 100.percent
        )
    ) {
        Button(
            attrs = Modifier
                .borderRadius(r = 15.px)
                .fontFamily(FONT_FAMILY)
                .border(
                    style = LineStyle.Solid,
                    color = Colors.White
                )
                .onClick {
                    onPrevious()
                }
                .toAttrs()
        ) {
            Text("Previous")
        }

        P(
            attrs = Modifier
                .margin(leftRight = 10.px)
                .fontFamily(FONT_FAMILY)
                .fontSize(15.px)
                .toAttrs()
        ) {
            Text("${currentPage + 1} of $totalPages")
        }

        Button(
            attrs = Modifier
                .backgroundColor(Colors.PaleTurquoise)
                .borderRadius(r = 15.px)
                .border(
                    style = LineStyle.Solid,
                    color = Colors.White
                )
                .fontFamily(FONT_FAMILY)
                .boxShadow(
                    blurRadius = 10.px,
                    spreadRadius = 0.px,
                    color = Colors.LightGray,
                    offsetX = 0.px,
                    offsetY = 4.px
                )
                .onClick {
                    onNext()
                }
                .toAttrs()
        ) {
            Text("Next")
        }
    }
}

@Composable
fun CartHeader(
    onClearClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.px)
            .maxWidth(900.px)
            .margin(bottom = 30.px)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .borderRadius(topLeft = 10.px, topRight = 10.px)
                .padding(10.px),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            P(
                attrs = Modifier
                    .fontSize(24.px)
                    .color(Colors.Black)
                    .fontFamily(ROBOTO_SERIF)
                    .margin(0.px)
                    .toAttrs()
            ) {
                Text("Shopping Cart \uD83D\uDED2")
            }

            P(
                attrs = ClearTextStyle.toModifier()
                    .color(Colors.Black)
                    .cursor(Cursor.Pointer)
                    .fontFamily(ROBOTO_SERIF)
                    .fontSize(16.px)
                    .margin(0.px)
                    .onClick { onClearClick() }
                    .toAttrs()
            ) {
                Text("| Clear All Items")
            }
        }
    }
}

@Composable
fun CheckoutSection(
    orderId: Int,
    firebaseUid: String,
    address: String,
    totalPrice: Double,
    isReadyToCheckout: Boolean,
    cartItem: List<CartItemDTO>,
    onCheckoutSuccess: (Int) -> Unit
) {
    val formattedTotal = totalPrice.asDynamic().toFixed(2) as String
    val coroutineScope = rememberCoroutineScope()
    var readyToCheckout by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        readyToCheckout = isShippingReady(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(900.px)
            .padding(20.px)
            .border(style = LineStyle.Solid, color = Colors.White)
            .margin(topBottom = 20.px)
            .backgroundColor(rgba(255, 255, 255, 0.8))
            .gap(16.px),
        horizontalAlignment = Alignment.CenterHorizontally, // center horizontally so it lines up
        verticalArrangement = Arrangement.Center
    ) {
        P(
            attrs = Modifier
                .fontFamily(FONT_FAMILY)
                .fontWeight(FontWeight.Bold)
                .fontSize(20.px)
                .margin(0.px)
                .toAttrs()
        ) {
            Text("Total: ₱$formattedTotal")
        }

        Button(
            attrs = Modifier
                .width(250.px)
                .height(40.px)
                .fontSize(14.px)
                .borderRadius(12.px)
                .fontFamily(FONT_FAMILY)
                .cursor(if (readyToCheckout) Cursor.Pointer else Cursor.NotAllowed)
                .backgroundColor(if (readyToCheckout) Colors.White else Colors.LightGray)
                .color(Colors.Black)
                .onClick {
                    if (readyToCheckout) {
                        coroutineScope.launch {
                            val newOrderId = CheckoutService.checkout(firebaseUid, address, cartItem)
                            if (newOrderId != null) {
                                console.log("✅ Order placed: ID $newOrderId")
                                onCheckoutSuccess(newOrderId)
                            } else {
                                console.error("❌ Failed to checkout")
                            }
                        }
                    }
                }
                .thenIf(!readyToCheckout) {
                    Modifier.disabled()
                }
                .toAttrs()
        ) {
            Text("Proceed to Checkout")
        }

        if (!readyToCheckout) {
            P(
                attrs = Modifier
                    .margin(top = 10.px)
                    .color(Colors.Red)
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .toAttrs()
            ) {
                Text("Please complete all required fields before proceeding.")
            }
        }
    }
}

@Composable
fun TextInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(250.px) // consistent small field width
            .margin(bottom = 12.px)
            .fontFamily(FONT_FAMILY)
    ) {
        Label(
            attrs = Modifier
                .fontWeight(FontWeight.Medium)
                .fontSize(14.px)
                .margin(bottom = 4.px)
                .toAttrs()
        ) {
            Text(label)
        }

        TextInput(
            value = value,
            attrs = Modifier
                .color(Colors.Gray)
                .width(100.percent)
                .padding(4.px)
                .borderRadius(8.px)
                .fontSize(14.px)
                .toAttrs {
                    placeholder("Enter $label")
                    onInput { onValueChange(it.value) }
                }
        )
    }
}

@Composable
fun CityDropdown(selectedCity: String, onCityChange: (String) -> Unit) {
    val ncrCities = listOf(
        "Caloocan", "Las Piñas", "Makati", "Malabon", "Mandaluyong", "Manila",
        "Marikina", "Muntinlupa", "Navotas", "Parañaque", "Pasay", "Pasig",
        "Quezon City", "San Juan", "Taguig", "Valenzuela"
    )

    Column(
        modifier = Modifier
            .width(250.px)
            .margin(bottom = 12.px)
            .fontFamily(FONT_FAMILY)
    ) {
        Label(
            attrs = Modifier
                .fontWeight(FontWeight.Medium)
                .fontSize(14.px)
                .margin(bottom = 4.px)
                .toAttrs()
        ) {
            Text("City")
        }

        Select(
            attrs = Modifier
                .width(100.percent)
                .padding(4.px)
                .cursor(Cursor.Pointer)
                .borderRadius(8.px)
                .fontSize(14.px)
                .boxShadow(
                    blurRadius = 4.px,
                    color = Colors.LightGray,
                    offsetX = 0.px,
                    offsetY = 2.px
                )
                .fontFamily(FONT_FAMILY)
                .toAttrs {
                    onChange { event -> event.value?.let { onCityChange(it) } }
                }
        ) {
            Option(value = "", attrs = Modifier.cursor(Cursor.Pointer).toAttrs()) {
                Text("Select a City")
            }

            ncrCities.forEach { city ->
                Option(
                    value = city,
                    attrs = {
                        if (city == selectedCity) attr("selected", "selected")
                    }
                ) {
                    Text(city)
                }
            }
        }
    }
}

@Composable
fun DeliveryOptions(
    selectedOption: DeliveryOption?,
    onOptionSelected: (DeliveryOption) -> Unit
) {
    val options = DeliveryOption.entries.toTypedArray()
    val breakpoint = rememberBreakpoint()

    val isMobile = breakpoint <= Breakpoint.SM

    // Use sensible max widths — no 300.percent trick
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(
                when {
                    isMobile -> 100.percent
                    breakpoint <= Breakpoint.MD -> 600.px
                    else -> 900.px
                }
            )
            .backgroundColor(rgba(0, 0, 0, 0.5))
            .borderRadius(10.px)
            .fontFamily(FONT_FAMILY)
            .margin(top = 10.px)
            .gap(12.px)
            .padding(10.px)
    ) {
        Label(
            attrs = Modifier
                .fontWeight(FontWeight.Medium)
                .color(Colors.White)
                .fontSize(16.px)
                .margin(bottom = 6.px)
                .toAttrs()
        ) {
            Text("Delivery Method")
        }

        // layout: stacked on mobile, row on larger screens
        if (isMobile) {
            Column(modifier = Modifier.gap(12.px)) { // a little more breathing room
                options.forEach { option ->
                    DeliveryOptionRow(option, selectedOption == option) {
                        onOptionSelected(option)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .gap(16.px) // base spacing
                    .justifyContent(JustifyContent.SpaceEvenly) // spread them evenly
            ) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .flexGrow(1.0) // each option takes equal width
                            .onClick { onOptionSelected(option) }
                            .borderRadius(8.px)
                            .cursor(Cursor.Pointer)
                            .border(
                                style = LineStyle.Solid,
                                color = if (selectedOption == option) Colors.White else rgba(255, 255, 255, 0.2)
                            )
                            .padding(14.px) // a bit more padding to feel balanced
                    ) {
                        DeliveryOptionRow(
                            option = option,
                            isSelected = selectedOption == option,
                            onOptionSelected = { onOptionSelected(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryOptionRow(
    option: DeliveryOption,
    isSelected: Boolean,
    onOptionSelected: () -> Unit
) {
    Label(
        attrs = Modifier
            .display(DisplayStyle.Flex)
            .alignItems(AlignItems.Center) // vertical centering
            .gap(8.px) // consistent spacing
            .cursor(Cursor.Pointer)
            .toAttrs {
                onClick { onOptionSelected() }
            }
    ) {
        RadioInput(
            checked = isSelected,
            attrs = Modifier
                .margin(0.px) // reset default margins
                .toAttrs()
        )
        Span( // use Span instead of P to avoid extra margins
            attrs = Modifier
                .fontWeight(if (isSelected) FontWeight.Bold else FontWeight.Normal)
                .color(Colors.White)
                .toAttrs()
        ) {
            Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
        }
    }
}

enum class DeliveryOption {
    COURIER,
    SCHEDULED,
    PICKUP
}

@Composable
fun SelectCourierDropdown(
    selectedCourier: String,
    onCourierSelected: (String) -> Unit,
    courierOptions: List<String> = listOf("Lalamove", "Grab Express")
) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.SM

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(
                when {
                    isMobile -> 100.percent
                    breakpoint <= Breakpoint.MD -> 600.px
                    else -> 900.px
                }
            )
            .backgroundColor(rgba(0, 0, 0, 0.5))
            .borderRadius(10.px),
        contentAlignment = Alignment.TopStart
    ) {
        Div(attrs = Modifier.padding(10.px).classNames("dropdown").toAttrs()) {
            Button(
                attrs = Modifier
                    .backgroundColor(rgba(255, 255, 255, 0.7))
                    .color(Colors.Black)
                    .fontFamily(FONT_FAMILY)
                    .classNames("btn", "btn-secondary", "dropdown-toggle")
                    .toAttrs {
                        attr("data-bs-toggle", "dropdown")
                        attr("aria-expanded", "false")
                    }
            ) {
                Text(selectedCourier.ifBlank { "Select Courier" })
            }

            Ul(attrs = Modifier.classNames("dropdown-menu").margin(bottom = 10.px).toAttrs()) {
                courierOptions.forEach { courier ->
                    Li {
                        A(
                            attrs = Modifier
                                .classNames("dropdown-item")
                                .fontFamily(FONT_FAMILY)
                                .toAttrs {
                                    onClick { onCourierSelected(courier) }
                                }
                        ) {
                            Text(courier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PickUpSection(
    initialPhoneNumber: String,
    currentOrderId: Int?,
    setCurrentOrderId: (Int) -> Unit,
    firebaseUid: String
) {
    var pickupDate by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    val toastMessage = remember { mutableStateOf<String?>(null) }

    val today = remember {
        val jsDate = Date()
        val yyyy = jsDate.getFullYear()
        val mm = (jsDate.getMonth() + 1).toString().padStart(2, '0')
        val dd = jsDate.getDate().toString().padStart(2, '0')
        "$yyyy-$mm-$dd"
    }

    val defaultTime = "08:00"
    val hourOptions = (8..20).map { it.toString().padStart(2, '0') }
    val minuteOptions = listOf("00","05","10","15","20","25","30","35","40","45","50","55")
    var selectedHour by remember { mutableStateOf(defaultTime.split(":")[0]) }
    var selectedMinute by remember { mutableStateOf(defaultTime.split(":")[1]) }

    val scope = rememberCoroutineScope()
    val isPhoneValid = phoneNumber.length == 11 && phoneNumber.all { it.isDigit() }

    // Sync time
    LaunchedEffect(selectedHour, selectedMinute) { pickupTime = "$selectedHour:$selectedMinute" }

    // Set defaults
    LaunchedEffect(Unit) {
        if (pickupDate.isBlank()) pickupDate = today
        if (pickupTime.isBlank()) pickupTime = defaultTime
    }

    SimpleGrid(
        numColumns = numColumns(base = 1, md = 3),
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(900.px)
            .borderRadius(10.px)
            .margin(top = 10.px)
            .backgroundColor(rgba(0, 0, 0, 0.6))
            .gap(16.px)
            .padding(10.px)
    ) {
        // Phone Number Input
        Column(modifier = Modifier.width(250.px)) {
            Label(attrs = Modifier.fontFamily(FONT_FAMILY).color(Colors.White).fontWeight(FontWeight.Medium).fontSize(14.px).margin(bottom = 4.px).toAttrs()) {
                Text("Phone Number")
            }
            Input(type = InputType.Text) {
                value(phoneNumber)
                placeholder("09")
                onInput { phoneNumber = it.value }

                style {
                    width(100.percent)
                    borderRadius(15.px)
                    fontFamily(ROBOTO_SERIF)
                    fontSize(14.px)
                    padding(4.px)
                }
            }

            if (!isPhoneValid && phoneNumber.isNotBlank()) {
                P(
                    attrs = Modifier.color(Colors.White).fontSize(12.px).margin(top = 4.px).toAttrs()
                ) {
                    Text("Phone number must be 11 digits")
                }
            }
        }

        // Date Picker
        Column(modifier = Modifier.width(250.px)) {
            Label(attrs = Modifier.fontFamily(FONT_FAMILY).color(Colors.White).fontWeight(FontWeight.Medium).fontSize(14.px).margin(bottom = 4.px).toAttrs()) {
                Text("Pickup Date")
            }
            Input(type = InputType.Date) {
                value(pickupDate)
                min(today)
                onInput { pickupDate = it.value }

                style {
                    width(100.percent)
                    borderRadius(15.px)
                    fontFamily(ROBOTO_SERIF)
                    fontSize(14.px)
                    padding(4.px)
                }
            }

        }

        Column(modifier = Modifier.width(250.px)) {
            Label(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .color(Colors.White)
                    .fontWeight(FontWeight.Medium)
                    .fontSize(14.px)
                    .margin(bottom = 4.px)
                    .toAttrs()
            ) {
                Text("Pickup Time 08:00 - 20:00")
            }

            Row {
                // Hour dropdown
                Div(attrs = Modifier.classNames("dropdown").margin(right = 8.px).toAttrs()) {
                    Button(
                        attrs = Modifier.classNames("btn", "btn-secondary", "dropdown-toggle")
                            .toAttrs {
                                attr("data-bs-toggle", "dropdown")
                                attr("aria-expanded", "false")
                            }
                    ) {
                        Text(selectedHour)
                    }
                    Ul(attrs = Modifier.classNames("dropdown-menu","dropdown-width-50").toAttrs()) {
                        hourOptions.forEach { hour ->
                            Li {
                                A(
                                    attrs = Modifier.classNames("dropdown-item").fontFamily(ROBOTO_SERIF).toAttrs {
                                        onClick { selectedHour = hour }
                                    }
                                ) {
                                    Text(hour)
                                }
                            }
                        }
                    }
                }

                // Minute dropdown
                Div(attrs = Modifier.classNames("dropdown").toAttrs()) {
                    Button(
                        attrs = Modifier.classNames("btn", "btn-secondary", "dropdown-toggle")
                            .toAttrs {
                                attr("data-bs-toggle", "dropdown")
                                attr("aria-expanded", "false")
                            }
                    ) {
                        Text(selectedMinute)
                    }
                    Ul(attrs = Modifier.classNames("dropdown-menu", "dropdown-width-50").toAttrs()) {
                        minuteOptions.forEach { minute ->
                            Li {
                                A(
                                    attrs = Modifier.classNames("dropdown-item").fontFamily(ROBOTO_SERIF).toAttrs {
                                        onClick { selectedMinute = minute }
                                    }
                                ) {
                                    Text(minute)
                                }
                            }
                        }
                    }
                }
            }
        }
        // Submit Pickup Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .width(250.px)
                .margin(top = 16.px),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubmitPickupButton(
                scope = scope,
                phoneNumber = phoneNumber,
                currentOrderId = currentOrderId,
                setCurrentOrderId = setCurrentOrderId,
                firebaseUid = firebaseUid,
                toastMessage = toastMessage,
                enabled = isPhoneValid
            )

            // Toast message below the button
            toastMessage.value?.let { message ->
                P(
                    attrs = Modifier
                        .backgroundColor(rgba(0, 0, 0, 0.6))
                        .color(Colors.White)
                        .padding(6.px)
                        .margin(top = 8.px)
                        .borderRadius(6.px)
                        .toAttrs()
                )   {
                    Text(message)
                }

            }
        }
    }
}


@Composable
fun SubmitShippingButton(
    scope: CoroutineScope,
    selectedOption: DeliveryOption?,
    currentOrderId: Int?,
    setCurrentOrderId: (Int) -> Unit,
    firebaseUid: String,
    street: String,
    barangay: String,
    city: String,
    zipCode: String,
    selectedCourier: String?,
    phoneNumber: String,
    userShipping: HttpClient,
    toastMessage: MutableState<String?>
) {
    if (selectedOption != null) {
        Button(
            attrs = ButtonStyles.toModifier()
                .border(style = LineStyle.None)
                .margin(top = 15.px)
                .width(250.px)
                .fontSize(14.px)
                .height(40.px)
                .cursor(Cursor.Pointer)
                .borderRadius(r = 8.px)
                .fontFamily(FONT_FAMILY)
                .gap(16.px)
                .onClick {
                    scope.launch {
                        if (phoneNumber.length == 11 && phoneNumber.all { it.isDigit() }) {
                            try {
                                JsUserFetcher().updateUser(name = null, phone = phoneNumber)
                                console.log("✅ Phone number saved for user")
                            } catch (e: Exception) {
                                console.error("❌ Failed to save phone:", e.message)
                            }
                        }

                        toastMessage.value = "Sending shipping address..."

                        // Step 1: Create or reuse orderId
                        val orderId = currentOrderId ?: CheckoutService.checkout(
                            firebaseUid = firebaseUid,
                            address = listOf(street, barangay, city, zipCode)
                                .filter { it.isNotBlank() }
                                .joinToString(", "),
                            items = emptyList()
                        )

                        if (orderId == null) {
                            console.error("❌ Failed to create order")
                            toastMessage.value = "Failed to create order"
                            delay(3000)
                            toastMessage.value = null
                            return@launch
                        }

                        setCurrentOrderId(orderId)

                        // Step 2: Build shipping details
                        val shippingDetails = ShippingDetailsDTO(
                            orderId = orderId,
                            address = listOf(street, barangay, city, zipCode)
                                .filter { it.isNotBlank() }
                                .joinToString(", "),
                            courier = if (selectedOption == DeliveryOption.COURIER) selectedCourier else null,
                            trackingNumber = null,
                            status = ShippingStatus.Pending,
                            shippedAt = null,
                            deliveredAt = null
                        )

                        // Step 3: Call upsert API
                        val updatedShipping = upsertShippingDetail(shippingDetails)

                        if (updatedShipping != null) {
                            console.log("✅ Shipping details submitted:", updatedShipping)
                            toastMessage.value = "Shipping details received. We’ll get back to you with courier rates shortly."
                        } else {
                            console.error("❌ Failed to submit shipping")
                            toastMessage.value = "Failed to submit shipping"
                        }

                        delay(3000)
                        toastMessage.value = null
                    }
                }
                .toAttrs()
        ) {
            Text("Send Delivery Details")
        }
    }
}

@Composable
fun SubmitPickupButton(
    scope: CoroutineScope,
    phoneNumber: String,
    currentOrderId: Int?,
    setCurrentOrderId: (Int) -> Unit,
    firebaseUid: String,
    toastMessage: MutableState<String?>,
    enabled: Boolean
) {
    Button(
        attrs = Modifier
            .borderRadius(8.px)
            .margin(top = 15.px)
            .width(250.px)
            .height(40.px)
            .cursor(if (enabled) Cursor.Pointer else Cursor.Default)
            .fontFamily(FONT_FAMILY)
            .fontSize(14.px)
            .toAttrs {
                if (!enabled) disabled()  // <-- disable here
                onClick {
                    if (!enabled) return@onClick
                    scope.launch {
                        toastMessage.value = "Creating pickup..."
                        try {
                            val orderId = currentOrderId ?: CheckoutService.checkout(
                                firebaseUid = firebaseUid,
                                address = "",
                                items = emptyList()
                            )
                            if (orderId == null) {
                                toastMessage.value = "Failed to create order."
                                delay(3000)
                                toastMessage.value = null
                                return@launch
                            }
                            setCurrentOrderId(orderId)
                            val pickup = PickupApi.createPickup(orderId, phoneNumber)
                            toastMessage.value = if (pickup != null) {
                                "Pickup created successfully!"
                            } else {
                                "Failed to create pickup."
                            }
                        } catch (e: Exception) {
                            console.error("Error creating pickup:", e)
                            toastMessage.value = "Error: ${e.message}"
                        }
                        delay(5000)
                        toastMessage.value = null
                    }
                }
            }
    ) {
        Text("Submit Pickup")
    }
}