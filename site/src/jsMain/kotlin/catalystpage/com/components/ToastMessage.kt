package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.util.Constants.ROBOTO_SERIF
import com.varabyte.kobweb.compose.css.JustifyContent
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun ToastMessage(message: String?) {
    if (message != null) {
        Box(
            modifier = Modifier
                .position(Position.Fixed)
                .top(20.px)
                .left(0.px)
                .right(0.px)
                .zIndex(9999)
                .display(DisplayStyle.Flex)
                .justifyContent(JustifyContent.Center)
        ) {
            Div(
                attrs = Modifier
                    .backgroundColor(rgba(0, 0, 0, 0.6)) // translucent black
                    .color(Colors.WhiteSmoke)
                    .padding(12.px)
                    .borderRadius(8.px)
                    .fontFamily(ROBOTO_SERIF)
                    .fontSize(14.px)
                    .textAlign(TextAlign.Start)
                    .toAttrs()
            ) {
                Text(message)
            }
        }
    }
}