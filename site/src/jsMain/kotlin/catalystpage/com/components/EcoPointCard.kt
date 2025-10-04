package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.util.getNextBadgeThreshold
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import dto.BadgeDTO
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun EcoPointCard(
    ecoPoints: Int,
    allBadges: List<BadgeDTO>,
    modifier: Modifier = Modifier
) {
    // Determine next badge threshold
    val nextThreshold = getNextBadgeThreshold(ecoPoints, allBadges) ?: ecoPoints

    // Calculate progress percentage (0..1)
    val progressPercent = (ecoPoints.toFloat() / nextThreshold).coerceAtMost(1f)

    Box(
        modifier = modifier
            .width(380.px)
            .borderRadius(16.px)
            .backgroundColor(rgba(0, 0, 0, 0.4))
            .padding(16.px)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // Title
            P(
                attrs = Modifier
                    .fontWeight(FontWeight.Bold)
                    .fontSize(20.px)
                    .color(Colors.White)
                    .margin(bottom = 12.px)
                    .toAttrs()
            ) { Text("Your Eco Points") }

            // Points display
            P(
                attrs = Modifier
                    .fontSize(16.px)
                    .color(Colors.White)
                    .margin(bottom = 12.px)
                    .toAttrs()
            ) { Text("You have $ecoPoints points") }

            // Progress bar (fixed width 300px)
            Box(
                modifier = Modifier
                    .width(300.px)
                    .height(16.px)
                    .borderRadius(8.px)
                    .backgroundColor(rgba(255, 255, 255, 0.2))
            ) {
                Box(
                    modifier = Modifier
                        .width((progressPercent * 300).px)
                        .height(16.px)
                        .borderRadius(8.px)
                        .backgroundColor(Colors.PaleTurquoise)
                )
            }

            // Hint for next badge
            P(
                attrs = Modifier
                    .fontSize(12.px)
                    .color(Colors.White)
                    .margin(top = 8.px)
                    .toAttrs()
            ) {
                val remaining = (nextThreshold - ecoPoints).coerceAtLeast(0)
                Text("Only $remaining points to your next badge!")
            }
        }
    }
}