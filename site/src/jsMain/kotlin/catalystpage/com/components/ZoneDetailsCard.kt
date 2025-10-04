package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.model.Zone
import com.varabyte.kobweb.compose.css.FontSize
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ZoneDetailsCard(
    zones: List<Zone>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(400.px) // You can tweak this height based on your layout
            .overflow(Overflow.Auto) // Enables vertical scrolling
            .borderRadius(topLeft = 16.px, bottomLeft = 16.px)
            .backgroundColor(rgba(0, 0, 0, 0.6))
            .padding(16.px)
            .styleModifier {
                // Optional scrollbar styling
                property("scrollbar-width", "thin")
                property("scrollbar-color", "rgba(255,255,255,0.3) rgba(255,255,255,0.1)")
            },
        verticalArrangement = Arrangement.spacedBy(12.px)
    ) {
        P(
            attrs = Modifier
                .fontSize(18.px)
                .fontWeight(FontWeight.Bold)
                .color(Colors.White)
                .toAttrs()
        ) {
            Text("Zone Details")
        }

        zones.forEach { zone ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                P(
                    attrs = Modifier
                        .fontSize(16.px)
                        .fontWeight(FontWeight.SemiBold)
                        .color(Colors.White)
                        .toAttrs()
                ) { Text(zone.name) }

                // Horizontal scroll for cities
                Row(
                    modifier = Modifier
                        .display(DisplayStyle.Flex)
                        .gap(8.px)
                        .styleModifier {
                            property("overflow-x", "auto")
                            property("white-space", "nowrap")
                        }
                ) {
                    zone.cities.forEach { city ->
                        Box(
                            modifier = Modifier
                                .backgroundColor(rgba(255, 255, 255, 0.1))
                                .borderRadius(15.px)
                        ) {
                            P(
                                attrs = Modifier
                                    .color(Colors.White)
                                    .fontSize(14.px)
                                    .toAttrs()
                            ) {
                                Text(city)
                            }
                        }
                    }
                }
            }
        }
    }
}