package catalystpage.com.components

import admin.dto.RecyclingScheduleDTO
import admin.dto.ScheduleType
import androidx.compose.runtime.Composable
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date

@Composable
fun RecyclingScheduleCard(
    schedule: RecyclingScheduleDTO,
    modifier: Modifier = Modifier
) {
    val jsDate = Date(schedule.scheduleDate)

    val month = jsDate.toLocaleString("en-US", dateLocaleOptions {
        month = "long" // e.g. "October"
    })
    val day = jsDate.getDate() // e.g. 1

    val breakpoint = rememberBreakpoint()
    val isMobile = breakpoint <= Breakpoint.MD

    Box(
        modifier = modifier
            .width(if (isMobile) 400.px else 200.px)
            .height(250.px)
            .borderRadius(16.px)
            .backgroundColor(rgba(0, 0, 0, 0.3))
            .padding(16.px)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.SemiBold)
                    .fontSize(18.px)
                    .color(Colors.White)
                    .toAttrs()
            ) {
                Text(month.uppercase())
            }

            // Day number
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.Bold)
                    .fontSize(80.px)
                    .color(Colors.White)
                    .toAttrs()
            ) {
                Text(day.toString())
            }

            // Dynamic schedule type (RECYCLING or SHIPPING)
            P(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(15.px)
                    .color(Colors.White)
                    .toAttrs()
            ) {
                Text(
                    when (schedule.type) {
                        ScheduleType.RECYCLING -> "Recycling Day"
                        ScheduleType.SHIPPING -> "Shipping Day"
                    }
                )
            }
        }
    }
}