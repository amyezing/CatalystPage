package catalystpage.com.sections

import androidx.compose.runtime.*
import catalystpage.com.components.SectionTitle
import catalystpage.com.components.ValueBar
import catalystpage.com.model.Section
import catalystpage.com.model.Theme
import catalystpage.com.model.Values
import catalystpage.com.styles.AboutImageStyle
import catalystpage.com.styles.AboutTextStyle
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Constants.SECTION_WIDTH
import catalystpage.com.util.ObserveViewportEntered
import catalystpage.com.util.Res
import catalystpage.com.util.animateNumbers
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun AboutSection() {
    Box(
        modifier = Modifier
            .id(Section.About.id)
            .maxWidth(SECTION_WIDTH.px)
            .padding(topBottom = 150.px),
        contentAlignment = Alignment.Center
    ) {
        AboutContent()
    }
}

@Composable
fun AboutContent() {
    val breakpoint = rememberBreakpoint()
    Column(
        modifier = Modifier
            .fillMaxWidth(
                if (breakpoint >= Breakpoint.MD) 100.percent
                else 90.percent
            )
            .maxWidth(1200.px),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleGrid(
            modifier = Modifier.fillMaxWidth(
                if (breakpoint >= Breakpoint.MD) 90.percent
                else 100.percent
            ),
            numColumns = numColumns(base = 1, md = 2)
        ) {
            if (breakpoint >= Breakpoint.MD) {
                AboutImage()
            }
            AboutMe()
        }
    }
}

@Composable
fun AboutImage() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = AboutImageStyle.toModifier()
                .fillMaxWidth(80.percent)
                .fillMaxHeight(90.percent),
            src = Res.Image.about_image,
            alt = "About Image"
        )
    }
}

@Composable
fun AboutMe() {
    val scope = rememberCoroutineScope()
    var viewportEntered by remember { mutableStateOf(false) }
    val animatedPercentage = remember { mutableStateListOf(0, 0, 0, 0, 0) }

    ObserveViewportEntered(
        sectionId = Section.About.id,
        distanceFromTop = 300.0,
        onViewportEntered = {
            viewportEntered = true
            Values.entries.forEach { values ->
                scope.launch {
                    animateNumbers(
                        number = values.percentage.value.toInt(),
                        onUpdate = {
                            animatedPercentage[values.ordinal] = it
                        }
                    )
                }
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        SectionTitle(section = Section.About)
        P(
            attrs = AboutTextStyle.toModifier()
                .margin(topBottom = 25.px)
                .maxWidth(500.px)
                .fontFamily(FONT_FAMILY)
                .fontSize(18.px)
                .fontWeight(FontWeight.Normal)
                .fontStyle(FontStyle.Italic)
                .color(Color.darkgray)
                .toAttrs()
        ) {
            Text("At Catalyst, we believe in the transformative power of fermentation. Our handcrafted kombucha is more than just a drink—it's a commitment to balance, growth, and well-being. We focus on quality, sustainability, and innovation to bring you beverages that nourish both body and mind.")
        }
        Values.entries.forEach { skill ->
            ValueBar(
                title = skill.title,
                index = skill.ordinal,
                percentage = if (viewportEntered) skill.percentage else 0.percent,
                animatedPercentage = if (viewportEntered) animatedPercentage[skill.ordinal] else 0
            )
        }
    }
}