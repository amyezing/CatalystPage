package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.styles.BadgesStyle
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.style.toModifier
import dto.BadgeDTO
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun BadgeCard(
    badges: List<BadgeDTO>,
    modifier: Modifier = Modifier
) {
    var selectedBadge by remember { mutableStateOf<BadgeDTO?>(null) }

    Box(
        modifier = modifier
            .width(380.px)
            .borderRadius(16.px)
            .backgroundColor(rgba(0, 0, 0, 0.4))
            .padding(16.px)
    ) {
        Column {
            P(attrs = Modifier.fontWeight(FontWeight.Bold).fontSize(20.px).color(Colors.White).margin(bottom = 12.px).toAttrs()) {
                Text("Your Badges")
            }

            Row(modifier = Modifier.fillMaxWidth().gap(12.px)) {
                badges.forEach { badge ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(60.px)
                                .height(60.px)
                                .borderRadius(12.px)
                                .backgroundColor(
                                    Colors.PaleTurquoise.copy(alpha = if (badge.unlocked) 255 else 76)
                                )
                                .styleModifier {
                                    property("display", "flex")
                                    property("justify-content", "center")
                                    property("align-items", "center")
                                    property("cursor", if (badge.unlocked) "pointer" else "default")
                                    property("transition", "all 0.3s ease")
                                    if (badge.unlocked) property("box-shadow", "0 0 10px 2px rgba(255,255,255,0.7)")
                                }
                                .onClick { if (badge.unlocked) selectedBadge = badge }
                        ) {
                            if (badge.iconUrl.isNotEmpty()) {
                                Image(
                                    src = badge.iconUrl,
                                    alt = badge.name,
                                    modifier = Modifier.width(60.px).height(60.px)
                                )
                            }
                        }

                        P(
                            attrs = Modifier
                                .color(Colors.White)
                                .fontSize(12.px)
                                .textAlign(TextAlign.Center)
                                .margin(top = 4.px)
                                .toAttrs()
                        ) {
                            Text(badge.name)
                        }

                    }
                }
            }
        }

        selectedBadge?.let { badge ->
            Box(
                modifier = Modifier
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .width(100.percent)
                    .height(100.percent)
                    .backgroundColor(rgba(0, 0, 0, 0.7))
                    .styleModifier {
                        property("display", "flex")
                        property("justify-content", "center")
                        property("align-items", "center")
                        property("perspective", "600px") // add perspective
                    }
                    .onClick { selectedBadge = null }
            ) {
                Img(
                    src = badge.iconUrl,
                    attrs = BadgesStyle.toModifier()
                        .width(200.px)
                        .height(200.px)
                        .toAttrs()
                )
            }
        }
    }
}