package catalystpage.com.dashboards

import admin.dto.RecyclingScheduleDTO
import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.EcoAdminFetcher
import catalystpage.com.admin.fetcher.ScheduleFetcher
import catalystpage.com.components.*
import catalystpage.com.database.BadgeFetcher
import catalystpage.com.database.JsUserFetcher
import catalystpage.com.database.UEPFetcher
import catalystpage.com.model.ncrZones
import catalystpage.com.styles.ButtonStyles
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.AlignItems
import com.varabyte.kobweb.compose.css.JustifyContent
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.modifiers.alignItems
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.FaXmark
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.BadgeDTO
import dto.UserDTO
import dto.community.ZoneDTO
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement

@Composable
fun ProfileSection(zoneId: Int) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserDTO?>(null) }
    var badges by remember { mutableStateOf<List<BadgeDTO>>(emptyList()) }

    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD
    var userSchedules by remember { mutableStateOf<List<RecyclingScheduleDTO>>(emptyList()) } // for RecyclingScheduleCard
    var allSchedules by remember { mutableStateOf<List<RecyclingScheduleDTO>>(emptyList()) }
    var zones by remember { mutableStateOf<List<ZoneDTO>>(emptyList()) }

    // Fetch current user data + badges unlock

    LaunchedEffect(Unit) {
        scope.launch {
            val currentUser = JsUserFetcher().fetchCurrentUser()
            val ecoPoints = UEPFetcher.fetchUserEcoPoints(currentUser.id ?: 0)?.points ?: 0
            val updatedUser = currentUser.copy(ecoPoints = ecoPoints)
            user = updatedUser

            // 🔑 Unlock badges based on points
            updatedUser.firebaseUid.let { uid ->
                try {
                    val unlocked = BadgeFetcher.unlockBadgesByPoints(uid)
                    // Always refresh badge list whether unlocked or not
                    badges = BadgeFetcher.fetchUserBadges(uid)
                    console.log("✅ Badge unlock attempt result: $unlocked")
                } catch (e: Exception) {
                    console.error("❌ Error unlocking/fetching badges:", e)
                }
            }
            userSchedules = ScheduleFetcher.fetchByZone(zoneId)   // only user's zone
            allSchedules = ScheduleFetcher.fetchAll()
            zones = EcoAdminFetcher.getZones()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(1200.px)
            .padding(20.px)
            .display(DisplayStyle.Flex)
            .justifyContent(JustifyContent.Center)
    ) {
        Row(
            modifier = Modifier
                .display(DisplayStyle.Flex)
                .flexDirection(if (isMobile) FlexDirection.Column else FlexDirection.Row)
                .alignItems(AlignItems.Start),
            horizontalArrangement = Arrangement.spacedBy(20.px)
        ) {
            // ----------------- Left Column -----------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (isMobile) 20.px else 0.px),
                verticalArrangement = Arrangement.spacedBy(20.px)
            ) {
                Greetings()
                AccountSettings()
                ZoneDetailsCard(
                    zones = ncrZones,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ----------------- Middle Column -----------------
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.px),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProductStatusCard(
                    isBrewing = true,
                    modifier = Modifier.fillMaxWidth().height(250.px)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .display(DisplayStyle.Flex)
                        .flexDirection(if (isMobile) FlexDirection.Column else FlexDirection.Row)
                        .gap(20.px)
                        .then(
                            if (isMobile) {
                                Modifier.justifyContent(JustifyContent.Center)
                                    .alignItems(AlignItems.Center)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    userSchedules.forEach { schedule ->
                        RecyclingScheduleCard(
                            schedule = schedule,
                            modifier = Modifier.weight(1f).height(250.px)
                        )
                    }

                    // Show all schedules in RecyclingAreasCard
                    RecyclingAreasCard(
                        zones = zones,
                        schedules = allSchedules,
                        modifier = Modifier.weight(1f).height(250.px)
                    )
                }

                user?.id?.let {
                    EcoQRButton(userId = it.toString())
                }
            }

            // ----------------- Right Column -----------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (isMobile) 20.px else 0.px),
                verticalArrangement = Arrangement.spacedBy(20.px),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                user?.ecoPoints?.let { points ->
                    EcoPointCard(
                        ecoPoints = points,
                        allBadges = badges,
                        modifier = Modifier.fillMaxWidth().height(150.px)
                    )
                }

                BadgeCard(
                    badges = badges,
                    modifier = Modifier.fillMaxWidth().height(250.px)
                )

                user?.let { u ->
                    RecyclingProgressCard(
                        userId = u.id ?: 0,
                        zoneId = u.zoneId ?: 0, // <-- assuming UserDTO has zoneId
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}



@Composable
fun Greetings() {
    var isLoading by remember { mutableStateOf(true) }
    var userDto by remember { mutableStateOf<UserDTO?>(null) }
    val userFetcher = remember { JsUserFetcher() }

    val greeting = remember {
        val hour = js("new Date().getHours()") as Int
        when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            in 18..21 -> "Good Evening,"
            else -> "Good Night,"
        }
    }

    val breakpoint = rememberBreakpoint()

    LaunchedEffect(Unit) {
        try {
            userDto = userFetcher.fetchCurrentUser()
        } catch (e: Throwable) {
            console.error("❌ Error fetching user:", e)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .borderRadius(r = 15.px)
            .fillMaxWidth()
            .color(Colors.White)
            .padding(10.px)
            .margin(bottom = 10.px)
            .backgroundColor(rgba(0, 0, 0, 0.2)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        if (isLoading) {
            Text("Loading...")
        } else {
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.Bold)
                    .color(Colors.White)
                    .fontSize(if (breakpoint <= Breakpoint.MD) 15.px else 20.px)
                    .toAttrs()
            ) {
                Text(greeting)
            }
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.Normal)
                    .color(Colors.White)
                    .fontSize(if (breakpoint <= Breakpoint.MD) 15.px else 20.px)
                    .margin(left = 5.px, bottom = 15.px)
                    .toAttrs()
            ) {
                Text(userDto?.name ?: "Guest")
            }
        }
    }
}

@Composable
fun AccountSettings() {
    val scope = rememberCoroutineScope()
    var userData by remember { mutableStateOf<UserDTO?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    // Fetch user data once
    LaunchedEffect(Unit) {
        scope.launch {
            userData = JsUserFetcher().fetchCurrentUser()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .margin(topBottom = 10.px)
            .borderRadius(10.px)
            .padding(16.px)
            .backgroundColor(rgba(0, 0, 0, 0.2)),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.px)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            P(
                attrs = Modifier
                    .fontSize(20.px)
                    .color(Colors.White)
                    .fontWeight(FontWeight.Bold)
                    .fontFamily(FONT_FAMILY)
                    .toAttrs()
            ) { Text("Profile") }

            if (!isEditing) {
                P(
                    attrs = ButtonStyles.toModifier()
                        .fontFamily(FONT_FAMILY)
                        .fontSize(10.px)
                        .padding(topBottom = 1.5.px, leftRight = 4.px)
                        .borderRadius(r = 10.px)
                        .backgroundColor(rgba(0, 0, 0, 0.4))
                        .color(Colors.White)
                        .cursor(Cursor.Pointer)
                        .onClick { isEditing = true }
                        .toAttrs()
                ) {
                    Text("Edit")
                }
            }
        }

        if (!isEditing) {
            userData?.let { user ->
                ProfileRow("Name:", user.name ?: "Not Set")
                user.email?.let { ProfileRow("Email:", it) }
                ProfileRow("Phone:", user.phone ?: "Not Provided")
            } ?: Text("Loading...")
        } else {
            var fullName by remember { mutableStateOf(userData?.name ?: "") }
            var phone by remember { mutableStateOf(userData?.phone ?: "") }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.px)
            ) {
                Input(
                    InputType.Text,
                    attrs = Modifier
                        .borderRadius(r = 10.px)
                        .width(200.px)
                        .fontFamily(FONT_FAMILY)
                        .toAttrs {
                            value(fullName)
                            placeholder("Enter full name")
                            onInput { fullName = (it.target as HTMLInputElement).value }
                        }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.px)
                ) {
                    Input(
                        InputType.Tel,
                        attrs = Modifier
                            .borderRadius(r = 10.px)
                            .width(200.px)
                            .fontFamily(FONT_FAMILY)
                            .toAttrs {
                                value(phone)
                                placeholder("Enter phone number")
                                onInput { phone = (it.target as HTMLInputElement).value }
                            }
                    )

                    if (phone.isNotBlank()) {
                        FaXmark(
                            modifier = Modifier
                                .cursor(Cursor.Pointer)
                                .color(rgba(1.0, 1.0, 1.0, 0.6))
                                .onClick {
                                    console.log("🗑️ Clear phone clicked")
                                    phone = "" // just clear, backend will set null on save
                                },
                            size = IconSize.LG
                        )
                    }
                }

                Button(
                    attrs = Modifier
                        .borderRadius(10.px)
                        .fontFamily(FONT_FAMILY)
                        .border(style = LineStyle.None)
                        .backgroundColor(Colors.White)
                        .toAttrs {
                            onClick {
                                console.log("🔘 Save button clicked")
                                scope.launch {
                                    try {
                                        console.log("🚀 Starting updateUserDTO...")
                                        val fetcher = JsUserFetcher()
                                        fetcher.updateUser(
                                            fullName,
                                            if (phone.isBlank()) null else phone
                                        )
                                        console.log("📦 Fetching updated user...")
                                        userData = fetcher.fetchCurrentUser()
                                        isEditing = false
                                    } catch (e: Exception) {
                                        console.error("❌ Error during update flow:", e.message)
                                    }
                                }
                            }
                        }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row {
        P(
            attrs = Modifier
                .width(100.px)
                .color(Colors.White)
                .fontFamily(FONT_FAMILY)
                .fontWeight(FontWeight.Medium)
                .toAttrs()
        ) { Text(label) }

        P(
            attrs = Modifier
                .color(Colors.White)
                .fontFamily(FONT_FAMILY)
                .toAttrs()
        ) { Text(value) }
    }
}