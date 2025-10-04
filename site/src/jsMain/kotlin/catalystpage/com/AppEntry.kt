package catalystpage.com

import androidx.compose.runtime.*
import catalystpage.com.firebase.initFirebase
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.*

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {

    LaunchedEffect(Unit) {
        try {
            initFirebase() // ensures FirebaseConfig.firebaseApp exists early
        } catch (e: dynamic) {
            console.error("Firebase init failed at app start", e)
        }
    }

    SilkApp {
        Surface(SmoothColorStyle.toModifier().minHeight(100.vh)) {
            content()
        }
    }
}