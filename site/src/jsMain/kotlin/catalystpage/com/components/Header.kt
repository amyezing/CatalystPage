package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.model.Section
import catalystpage.com.styles.LogoStyle
import catalystpage.com.styles.NavigationItemStyle
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Res
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaBars
import com.varabyte.kobweb.silk.components.icons.fa.FaUser
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

@Composable
fun Header(onMenuClicked: () -> Unit) {
    val breakpoint = rememberBreakpoint()
    Row(
        modifier = Modifier
            .fillMaxWidth(if (breakpoint > Breakpoint.MD) 80.percent else 90.percent)
            .margin(topBottom = 50.px),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeftSide(
            breakpoint = breakpoint,
            onMenuClicked = onMenuClicked
        )
        if (breakpoint > Breakpoint.MD) {
            RightSide()
        }
    }
}

@Composable
fun LeftSide(
    breakpoint: Breakpoint,
    onMenuClicked: () -> Unit
) {
    val pageContext = rememberPageContext()
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (breakpoint <= Breakpoint.MD) {
            FaBars(
                modifier = Modifier
                    .margin(right = 15.px)
                    .color(Colors.White)
                    .onClick {
                        onMenuClicked()
                    },
                size = IconSize.XL
            )
        }
       Image(
            modifier = LogoStyle.toModifier()
                .size(55.px)
                .cursor(Cursor.Pointer)
                .onClick {
                    pageContext.router.navigateTo("/#home")
                },
            src = Res.Image.logo,
            alt = "Logo Image"
        )
    }
}

@Composable
fun RightSide() {
    val pageContext = rememberPageContext()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.px)
            .backgroundColor(Colors.DimGray)
            .borderRadius(r = 50.px),
        horizontalArrangement = Arrangement.End
    ) {
        Section.entries.toTypedArray().take(6).forEach { section ->
            Link(
                modifier = NavigationItemStyle.toModifier()
                    .padding(right = 30.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Normal)
                    .textDecorationLine(TextDecorationLine.None),
                path = section.path,
                text = section.title
            )
        }
        FaUser(
            modifier = NavigationItemStyle.toModifier()
                .margin(10.px)
                .cursor(Cursor.Pointer)
                .onClick {
                    pageContext.router.navigateTo("/signIn")
                },
            size = IconSize.LG
        )
    }
}