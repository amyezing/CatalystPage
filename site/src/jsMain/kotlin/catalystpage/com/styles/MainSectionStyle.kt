package catalystpage.com.styles

import catalystpage.com.model.Theme
import catalystpage.com.util.Constants.ROBOTO_SERIF
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.StyleScope
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.css.*


val NavigationItemStyle = CssStyle {
    base {
        Modifier
            .styleModifier {
                property("--bs-link-color", Theme.BrightGray.rgb)
            }
            .color(Theme.BrightGray.rgb)
            .transition(Transition.of(property = "color", duration = 200.ms))
    }
    hover {
        Modifier
            .styleModifier {
                property("--bs-link-hover-color", Colors.Black)
            }
            .color(Colors.Black)
    }
}

val LogoStyle = CssStyle {
    base {
        Modifier
            .transform { rotate(0.deg) }
            .transition(Transition.of(property = "transform", duration = 2000.ms))
    }
    hover {
        Modifier
            .transform { rotate((360).deg) }
    }
}

val SocialLinkStyle = CssStyle {
    base {
        Modifier
            .color(Color.dimgray)
            .transition(Transition.of(property = "color", duration = 200.ms))
    }
    hover {
        Modifier.color(Colors.Black)
    }
}

val MainButtonStyle = CssStyle {
    base {
        Modifier
            .width(100.px)
            .transition(Transition.of(property = "width", duration = 200.ms))
    }
    hover {
        Modifier.width(120.px)
    }
}

val BadgesStyle = CssStyle {
    base {
        Modifier
            .transform { rotateY(0.deg) }
            .transition(Transition.of(property = "transform", duration = 2000.ms))
    }
    hover {
        Modifier
            .transform { rotateY((360).deg) }
    }
}

val ClearTextStyle = CssStyle {
    base {
        Modifier
            .fontStyle(FontStyle.Italic)
            .transition(Transition.of(property = "transform", duration = 2000.ms))

    }
    hover {
        Modifier
            .fontStyle(FontStyle.Normal) // change style on hover
            .textDecorationLine(TextDecorationLine.Underline) // optional
    }
}

