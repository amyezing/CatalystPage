package catalystpage.com.styles

import com.varabyte.kobweb.compose.css.TransitionDelay
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.transformStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.css.*

val ButtonStyle = CssStyle {
    base {
        Modifier
            .styleModifier {
                property("--bn-color", Colors.LightGray)
            }
            .transition(Transition.of(property = "color", duration = 200.ms))

    }
    hover {
        Modifier
            .styleModifier {
                property("--bn-color-hover-color", Colors.DimGray)
            }
            .color(Colors.DimGray)
    }
}

