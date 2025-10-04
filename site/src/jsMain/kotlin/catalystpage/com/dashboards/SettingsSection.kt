package catalystpage.com.dashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.UserFetcher
import catalystpage.com.components.ToggleSwitch
import catalystpage.com.database.JsUserFetcher
import catalystpage.com.database.NotificationFetcher
import catalystpage.com.database.NotificationFetcher.createNotificationSettings
import catalystpage.com.database.NotificationFetcher.fetchNotificationSettings
import catalystpage.com.database.NotificationFetcher.updateNotificationSettings
import catalystpage.com.styles.ButtonStyles
import catalystpage.com.styles.InputStyle
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.UpdateSettingsDTO
import dto.UserDTO
import dto.UserNotificationDTO
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLInputElement

@Composable
fun SettingsSection(firebaseUid: String) {
    val breakpoint = rememberBreakpoint()
    var currentUser by remember { mutableStateOf<UserNotificationDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(firebaseUid) {
        try {
            isLoading = true
            createNotificationSettings(firebaseUid) // ensure defaults
            currentUser = fetchNotificationSettings(firebaseUid)
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    // 🔹 Outer wrapper centers content
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(top = 40.px, bottom = 40.px)
            .fontFamily(FONT_FAMILY),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .maxWidth(
                    when {
                        breakpoint <= Breakpoint.SM -> 320.px   // mobile
                        breakpoint <= Breakpoint.MD -> 480.px   // tablet
                        else -> 600.px                           // desktop
                    }
                )
                .fillMaxWidth()
                .padding(leftRight = 16.px) // safe spacing on small screens
        ) {
            Box(
                modifier = Modifier
                    .borderRadius(r = 15.px)
                    .padding(20.px)
                    .fillMaxWidth()
                    .backgroundColor(rgba(255, 255, 255, 0.6)),
                contentAlignment = Alignment.TopStart
            ) {
                Column {
                    DeleteUser(
                        onDelete = {
                            window.alert("Account deleted. Redirecting...")
                            window.location.href = "/"
                        }
                    )

                    when {
                        isLoading -> P { Text("Loading settings...") }
                        error != null -> P { Text("Error: $error") }
                        currentUser != null -> NotificationPreferences(
                            firebaseUid = firebaseUid,
                            settings = currentUser!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ContactSupportButton()
                    SupportForm(firebaseUid = firebaseUid, breakpoint = breakpoint)
                    AppInfo()
                }
            }
        }
    }
}


@Composable
fun DeleteUser(onDelete: () -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }

    Column {
        Button(
            attrs = ButtonStyles.toModifier()
                .border(color = Colors.White, style = LineStyle.Solid, width = 0.5.px)
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .fontWeight(FontWeight.Bold)
                .padding(10.px)
                .borderRadius(15.px)
                .onClick { showConfirm = true }
                .toAttrs()
        ) {
            Text("Delete Account")
        }

        // 🔔 Confirmation Toast/Dialog
        if (showConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .backgroundColor(rgba(0, 0, 0, 0.6)) // dark overlay
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .zIndex(999),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .backgroundColor(Colors.White)
                        .padding(20.px)
                        .borderRadius(15.px)
                        .maxWidth(400.px),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.px)
                ) {
                    P(
                        attrs = Modifier
                            .color(Colors.Red)
                            .fontWeight(FontWeight.Bold)
                            .toAttrs()
                    ) {
                        Text("⚠️ Are you sure you want to delete your account?")
                    }

                    P(
                        attrs = Modifier
                            .color(Colors.Red)
                            .toAttrs()
                    ) {
                        Text("This action cannot be undone.")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.px),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            attrs = Modifier
                                .backgroundColor(Colors.Red)
                                .color(Colors.White)
                                .borderRadius(10.px)
                                .padding(8.px)
                                .onClick {
                                    coroutineScope.launch {
                                        val deleted = JsUserFetcher().deleteCurrentUser()
                                        if (deleted) {
                                            window.localStorage.clear()
                                            onDelete()
                                        } else {
                                            console.error("❌ Deletion failed.")
                                        }
                                    }
                                    showConfirm = false
                                }
                                .toAttrs()
                        ) {
                            Text("Yes, Delete")
                        }

                        Button(
                            attrs = Modifier
                                .backgroundColor(Colors.LightGray)
                                .borderRadius(10.px)
                                .padding(8.px)
                                .onClick { showConfirm = false }
                                .toAttrs()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPreferences(
    firebaseUid: String,
    settings: UserNotificationDTO,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var localSettings by remember { mutableStateOf(settings) }

    Column(modifier = modifier.padding(16.px)) {
        P(attrs = Modifier.fontWeight(FontWeight.Bold).toAttrs()) {
            Text("Notification Settings")
        }

        // Order Updates: initially checked, can be disabled if required
        ToggleSwitch(
            id = "orderUpdates",
            checked = localSettings.notifyOrderUpdates,
            label = "Order Updates",
            onChange = { checked ->
                val prev = localSettings
                localSettings = localSettings.copy(notifyOrderUpdates = checked)

                scope.launch {
                    val ok = NotificationFetcher.updateNotificationSettings(
                        firebaseUid,
                        UpdateSettingsDTO(
                            notifyMarketing = localSettings.notifyMarketing,
                            notifyOrderUpdates = checked
                        )
                    )
                    if (!ok) localSettings = prev
                }
            }
        )

        // Marketing Notifications: initially checked, fully toggleable
        ToggleSwitch(
            id = "marketing",
            checked = localSettings.notifyMarketing,
            label = "Newsletter",
            onChange = { checked ->
                val prev = localSettings
                localSettings = localSettings.copy(notifyMarketing = checked)

                scope.launch {
                    val ok = NotificationFetcher.updateNotificationSettings(
                        firebaseUid,
                        UpdateSettingsDTO(
                            notifyMarketing = checked,
                            notifyOrderUpdates = localSettings.notifyOrderUpdates
                        )
                    )
                    if (!ok) localSettings = prev
                }
            }
        )
    }
}
@Composable
fun AppInfo() {
    Column(modifier = Modifier.margin(top = 16.px)) {
        Text("Catalyst Beverage Manufacturing")
        Text(" Version 1.0.0")
        Text("© 2025 Catalyst")
    }
}

@Composable
fun ContactSupportButton() {
    Button(
        attrs = Modifier
            .margin(topBottom = 8.px)
            .fontFamily(FONT_FAMILY)
            .borderRadius(10.px)
            .onClick {
                window.open("mailto:kombucha@catalystbeveragemanufacturing.com")
            }
            .toAttrs()
    ) {
        Text("📧 Contact Support")
    }
}


@Composable
fun SupportForm(firebaseUid: String, breakpoint: Breakpoint) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Form(
            action = "https://formspree.io/f/mayzkand",
            attrs = Modifier.attrsModifier {
                attr("method", "POST")
            }.toAttrs()
        ) {
            // 🔒 Hidden Firebase UID
            Input(
                type = InputType.Hidden,
                attrs = Modifier
                    .id("firebaseUid")
                    .attrsModifier {
                        attr("name", "firebaseUid")
                        attr("value", firebaseUid)
                    }
                    .toAttrs()
            )

            // 📝 Message TextArea
            Label(
                forId = "inputMessage",
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .toAttrs()
            ) {
                Text("Directly send us your concern")
            }

            TextArea(
                attrs = InputStyle.toModifier()
                    .id("inputMessage")
                    .height(100.px)
                    .margin(bottom = 20.px)
                    .width(if (breakpoint >= Breakpoint.MD) 300.px else 250.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .attrsModifier {
                        attr("placeholder", "How can we help you?")
                        attr("name", "message")
                        attr("required", "true")
                    }
                    .toAttrs()
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                Button(
                    attrs = ButtonStyles.toModifier()
                        .height(40.px)
                        .fontFamily(FONT_FAMILY)
                        .border(width = 0.px)
                        .borderRadius(5.px)
                        .backgroundColor(Colors.PaleTurquoise)
                        .color(Colors.Black)
                        .cursor(Cursor.Pointer)
                        .toAttrs()
                ) {
                    Text("Send Message")
                }
            }
        }
    }
}
