package catalystpage.com.styles

import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.rgba

val ButtonStyles = CssStyle {
    base {
        Modifier
            .color(Colors.White)
            .backgroundColor(rgba(0, 0, 0, 0.6))
            .transition(Transition.of(property = "color", duration = 200.ms))
    }
    hover {
        Modifier
            .color(Colors.Black)
            .backgroundColor(Color.paleturquoise)
    }
}