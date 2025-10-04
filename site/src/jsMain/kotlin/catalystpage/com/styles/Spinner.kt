package catalystpage.com.styles

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.styleModifier
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit

@Composable
fun Spinner(size: CSSSizeValue<CSSUnit.px>, modifier: Modifier = Modifier) {
    var rotation by remember { mutableStateOf(0f) }

    // Infinite rotation
    LaunchedEffect(Unit) {
        while (true) {
            rotation += 6f // rotation speed in degrees
            if (rotation >= 360f) rotation = 0f
            delay(16L) // ~60fps
        }
    }

    Box(
        modifier = modifier
            .width(size)
            .height(size)
            .styleModifier {
                property("border", "6px solid rgba(255,255,255,0.3)")
                property("border-top-color", "white")
                property("border-radius", "50%")
                property("transform", "rotate(${rotation}deg)")
                property("box-sizing", "border-box")
            }
    )
}