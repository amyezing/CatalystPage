package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.util.generateQRCodeDataUrl
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun EcoCycleCode(userId: String) {
    var qrUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        generateQRCodeDataUrl(userId) { url ->
            qrUrl = url
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        P(
            attrs = Modifier.fontWeight(FontWeight.Bold)
                .fontSize(16.px)
                .color(Colors.White)
                .margin(bottom = 8.px)
                .toAttrs()
        ) { Text("Your Eco Cycle Code") }

        qrUrl?.let {
            Img(
                src = it,
                alt = "Eco Cycle QR Code",
                attrs = Modifier
                    .width(200.px)
                    .height(200.px)
                    .toAttrs()
            )

        }
    }
}