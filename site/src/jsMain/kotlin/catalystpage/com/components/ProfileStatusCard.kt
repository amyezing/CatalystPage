package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.styles.Spinner
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.functions.LinearGradient
import com.varabyte.kobweb.compose.css.functions.linearGradient
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ProductStatusCard(
    isBrewing: Boolean,
    modifier: Modifier = Modifier
) {
    val imageUrl = if (isBrewing) {
        "https://storage.googleapis.com/catalyst-cloud-storage/20250826_225308.png"
    } else {
        "https://storage.googleapis.com/catalyst-cloud-storage/20250826_223447.png"
    }

    Box(
        modifier = modifier
            .width(400.px)
            .height(250.px)
            .borderRadius(16.px)
            .overflow(Overflow.Hidden)
            .backgroundColor(rgba(0, 0, 0, 0.4))
            .position(Position.Relative)
    ) {
        Image(
            src = imageUrl,
            alt = "Product Status",
            modifier = Modifier.fillMaxSize().objectFit(ObjectFit.Cover)
        )

        if (isBrewing) {
            Spinner(size = 60.px, modifier = Modifier.align(Alignment.Center))
        }

        P(
            attrs = Modifier
                .color(Colors.Black)
                .padding(4.px)
                .fontFamily("Century Gothic")
                .fontStyle(FontStyle.Italic)
                .borderRadius(r = 15.px)
                .backgroundColor(rgba(255, 255, 255, 0.8))
                .align(Alignment.BottomCenter)
                .fontSize(18.px)
                .fontWeight(FontWeight.Bold)
                .toAttrs()
        ) {
            Text(if (isBrewing) "We're brewing…" else "Now Available!")
        }
    }
}