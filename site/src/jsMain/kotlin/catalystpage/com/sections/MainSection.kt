package catalystpage.com.sections

import androidx.compose.runtime.Composable
import catalystpage.com.components.Header
import catalystpage.com.components.SocialBar
import catalystpage.com.model.Section
import catalystpage.com.model.Theme
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Constants.SECTION_WIDTH
import catalystpage.com.util.Res
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
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun MainSection(onMenuClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .id(Section.Home.id)
            .maxWidth(SECTION_WIDTH.px)
            .backgroundColor(Colors.DimGray),
        contentAlignment = Alignment.TopCenter
    ) {
        MainContent(onMenuClicked = onMenuClicked)
    }
}

@Composable
fun MainContent(onMenuClicked: () -> Unit) {
    val breakpoint = rememberBreakpoint()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(onMenuClicked = onMenuClicked)
        SimpleGrid(
            modifier = Modifier.fillMaxWidth(
                if(breakpoint >= Breakpoint.MD) 80.percent
                else 90.percent
            ),
            numColumns = numColumns(base = 1, md = 2)
        ) {
            MainText(breakpoint = breakpoint)
            MainImage()
        }
    }
}

@Composable
fun MainText(breakpoint: Breakpoint) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (breakpoint > Breakpoint.MD) {
            SocialBar()
        }
        Column {
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .padding(leftRight = 25.px)
                    .fontWeight(FontWeight.Bold)
                    .fontSize(
                        if (breakpoint >= Breakpoint.MD) 68.px
                        else if (breakpoint >= Breakpoint.LG) 50.px
                        else 40.px
                    )
                    .fontWeight(FontWeight.Normal)
                    .color(Theme.BrightGray.rgb)
                    .toAttrs()
            ) {
                Text("Balanced Brews,")
            }
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .padding(leftRight = 25.px)
                    .fontWeight(FontWeight.Medium)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(
                        if (breakpoint >= Breakpoint.MD) 68.px
                        else if (breakpoint >= Breakpoint.LG) 50.px
                        else 40.px
                    )
                    .fontWeight(FontWeight.Normal)
                    .color(Theme.BrightGray.rgb)
                    .toAttrs()
            ) {
                Text("For a Thriving Life")
            }
        }
    }
}

@Composable
fun MainImage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .margin(bottom = 50.px)
                .borderRadius(25.px),
            src = Res.Image.main_image,
            alt = "Main Image"
        )
    }
}