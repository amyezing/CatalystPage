package catalystpage.com.components

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun EcoQRButton(userId: String) {
    var showModal by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            attrs = Modifier
                .margin(bottom = 25.px)
                .width(300.px)
                .backgroundColor(rgba(0, 0, 0, 0.7))
                .color(Colors.White)
                .borderRadius(r = 12.px)
                .padding(14.px)
                .fontWeight(FontWeight.Medium)
                .onClick { showModal = true }
                .toAttrs()

        ) {
            Text("Generate Eco-Cycle")
        }

        if (showModal) {
            // Fullscreen overlay modal
            Box(
                modifier = Modifier
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .width(100.percent)
                    .height(100.percent)
                    .backgroundColor(rgba(0, 0, 0, 0.4))
                    .display(DisplayStyle.Flex)
                    .justifyContent(JustifyContent.Center)
                    .alignItems(AlignItems.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.px)
                ) {
                    // The actual QR code
                    EcoCycleCode(userId = userId)

                    Button(
                        attrs = Modifier
                            .border(style = LineStyle.None)
                            .borderRadius(r = 10.px)
                            .color(Colors.Black)
                            .backgroundColor(rgba(255, 255, 255, 0.8))
                            .onClick { showModal = false }
                            .toAttrs()
                    ){
                        Text("Close")
                    }
                }
            }
        }
    }
}