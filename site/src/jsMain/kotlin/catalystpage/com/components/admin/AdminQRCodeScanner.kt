package catalystpage.com.components.admin

import androidx.compose.runtime.*
import catalystpage.com.database.Html5Qrcode
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.width
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div


@Composable
fun AdminQRCodeScanner(onScan: (String) -> Unit) {
    var scannerInitialized by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.width(400.px).height(400.px)) {
        Div(attrs = {
            id("qr-reader")
            style {
                width(400.px)
                height(400.px)
                border(2.px, LineStyle.Solid, Color.gray) // optional: visible container
            }
        })

        LaunchedEffect(Unit) {
            if (!scannerInitialized) {
                val html5QrCode = Html5Qrcode("qr-reader")
                window.setTimeout({
                    html5QrCode.start(
                        js("{ facingMode: 'environment' }"),
                        js("{ fps: 10, qrbox: 300 }"),
                        { decodedText: String ->
                            scope.launch { onScan(decodedText) }
                            html5QrCode.stop()
                        },
                        { errorMessage: String ->
                            console.log("QR scan error: ", errorMessage)
                        }
                    )
                }, 800)
                scannerInitialized = true
            }
        }
    }
}