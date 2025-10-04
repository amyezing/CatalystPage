package catalystpage.com.components

import admin.dto.RecyclingScheduleDTO
import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import dto.community.ZoneDTO
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun RecyclingAreasCard(
    schedules: List<RecyclingScheduleDTO>,
    zones: List<ZoneDTO>,
    modifier: Modifier = Modifier
) {
    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    // Prepare areas text
    val areas = schedules.mapNotNull { s -> zones.find { it.id == s.zoneId }?.name }.distinct()

    // Format dates as MM-dd-yyyy
    val upcomingSchedules = schedules.map { s ->
        runCatching {
            val parts = s.scheduleDate.split("-") // assuming yyyy-MM-dd
            if (parts.size == 3) "${parts[1]}-${parts[2]}-${parts[0]}" else s.scheduleDate
        }.getOrElse { s.scheduleDate }
    }

    Column(
        modifier = modifier
            .width(if (isMobile) 400.px else 200.px)
            .borderRadius(16.px)
            .overflow(Overflow.Auto)
            .backgroundColor(rgba(0, 0, 0, 0.5))
            .padding(16.px)
            .styleModifier {
                property("scrollbar-width", "thin")
                property("scrollbar-color", "rgba(255,255,255,0.3) rgba(255,255,255,0.1)")
            },
        verticalArrangement = Arrangement.spacedBy(12.px),
        horizontalAlignment = Alignment.Start
    ) {
        // Title
        P(
            attrs = Modifier
                .fontSize(18.px)
                .fontWeight(FontWeight.Bold)
                .color(Colors.White)
                .margin(bottom = 6.px)
                .toAttrs()
        ) { Text("Recycling Areas:") }

        // Display areas
        P(
            attrs = Modifier
                .fontSize(14.px)
                .lineHeight(14.px)
                .color(Colors.White)
                .toAttrs()
        ) { Text(areas.joinToString(", ")) }

        // Upcoming Schedule Title
        P(
            attrs = Modifier
                .fontSize(18.px)
                .fontWeight(FontWeight.Bold)
                .color(Colors.White)
                .margin(top = 12.px, bottom = 6.px)
                .toAttrs()
        ) { Text("Upcoming Schedule") }

        // Display upcoming dates with zones
        Column {
            schedules.forEach { s ->
                val zoneName = zones.find { it.id == s.zoneId }?.name ?: "Zone ${s.zoneId}"
                val formattedDate = runCatching {
                    val parts = s.scheduleDate.split("-")
                    if (parts.size == 3) "${parts[1]}-${parts[2]}-${parts[0]}" else s.scheduleDate
                }.getOrElse { s.scheduleDate }

                P(
                    attrs = Modifier
                        .fontSize(14.px)
                        .lineHeight(14.px)
                        .color(Colors.White)
                        .toAttrs()
                ) { Text("$formattedDate ($zoneName)") }
            }
        }
    }
}