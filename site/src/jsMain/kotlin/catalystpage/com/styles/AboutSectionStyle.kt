package catalystpage.com.styles

import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.TransitionProperty
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*

val AboutTextStyle = CssStyle {
    base {
        Modifier
            .opacity(50.percent)
            .transition(Transition.of(property = "opacity", duration = 200.ms))
    }
    hover {
        Modifier.opacity(100.percent)
    }
}

@OptIn(ExperimentalComposeWebApi::class)
val AboutImageStyle = CssStyle {
    base {
        Modifier
            .styleModifier { filter { grayscale(100.percent) } }
            .borderRadius(r = 0.px)
            .transition(Transition.Companion.of(property = TransitionProperty.All, duration = 200.ms))
    }
    hover {
        Modifier
            .styleModifier { filter { grayscale(0.percent) } }
            .borderRadius(r = 25.px)
    }
}