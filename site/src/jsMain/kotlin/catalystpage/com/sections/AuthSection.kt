package catalystpage.com.sections

import androidx.compose.runtime.*
import catalystpage.com.components.Header
import catalystpage.com.model.Section
import catalystpage.com.screens.AuthScreen
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Res
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun AuthSection(onMenuClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .id(Section.SignIn.id)
            .fillMaxSize()
            .backgroundColor(Colors.DimGray),
        contentAlignment = Alignment.TopCenter
    ) {
        AuthContent(onMenuClicked = onMenuClicked)
    }
}

@Composable
fun AuthContent(onMenuClicked: () -> Unit) {
    val breakpoint = rememberBreakpoint()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(onMenuClicked = onMenuClicked)
        Row(
            modifier = Modifier
                .borderRadius(r = 10.px)
                .padding(20.px)
                .fillMaxWidth(if (breakpoint > Breakpoint.MD) 60.percent else 90.percent)
                .backgroundColor(Colors.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AuthScreen()
            AuthArt(breakpoint = breakpoint)
        }
    }
}


@Composable
fun AuthArt(breakpoint: Breakpoint) {
    var isVisible by remember { mutableStateOf(false) }
    var translateX by remember { mutableStateOf(20.px) }
    var translateY by remember { mutableStateOf(20.px) }

    LaunchedEffect(Unit) {
        isVisible = true
        translateX = 0.px
        translateY = 0.px
    }

    val hideOnMobile = breakpoint <= Breakpoint.SM
    val isTablet = breakpoint == Breakpoint.MD || breakpoint == Breakpoint.LG

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        // Main background image
        Image(
            modifier = Modifier
                .position(Position.Relative)
                .then(
                    when {
                        hideOnMobile -> Modifier.display(DisplayStyle.None)
                        else -> Modifier.maxSize(500.px) // full for desktop
                    }
                )
                .boxShadow(
                    offsetX = 1.px,
                    offsetY = 1.px,
                    blurRadius = 3.px,
                    spreadRadius = 1.px,
                    color = Colors.DimGray.copyf(alpha = 0.5f)
                )
                .borderRadius(r = 10.px),
            src = Res.Image.auth1,
            alt = "Image Background"
        )

        // Floating quote box
        if (!hideOnMobile) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .top(if (isTablet) 55.percent else 50.percent) // slight adjust
                    .left(if (isTablet) 45.percent else 50.percent)
                    .translateX(translateX)
                    .translateY(translateY)
                    .backgroundColor(Colors.PaleTurquoise)
                    .opacity(80.percent)
                    .padding(if (isTablet) 8.px else 12.px)
                    .border(style = LineStyle.None)
                    .borderRadius(r = 10.px)
                    .margin(if (isTablet) 15.px else 25.px)
                    .boxShadow(
                        offsetX = 0.px,
                        offsetY = 1.px,
                        blurRadius = 3.px,
                        spreadRadius = 1.px,
                        color = Colors.DimGray.copyf(alpha = 0.2f)
                    )
            ) {
                Column {
                    P(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontStyle(FontStyle.Italic)
                            .fontWeight(FontWeight.Bold)
                            .color(Colors.Black)
                            .fontSize(if (isTablet) 13.px else 15.px)
                            .margin(bottom = (-5).px)
                            .toAttrs()
                    ) {
                        Text("The process itself")
                    }
                    P(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontWeight(FontWeight.Thin)
                            .color(Colors.Black)
                            .fontSize(if (isTablet) 11.px else 12.px)
                            .toAttrs()
                    ) {
                        Span(
                            attrs = Modifier
                                .display(DisplayStyle.Block)
                                .margin(bottom = (-10).px)
                                .toAttrs()
                        ) { Text("is where meaning is found.") }
                    }
                }
            }
        }
    }
}
