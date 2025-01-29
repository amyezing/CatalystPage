package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.model.Article
import catalystpage.com.model.Theme
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ArticleCard(
    breakpoint: Breakpoint,
    active: Boolean = false,
    article: Article,
    animatedMargin: CSSSizeValue<CSSUnit.px>
) {
    SimpleGrid(
        modifier = Modifier
            .fillMaxWidth()
            .maxWidth(
                if (breakpoint >= Breakpoint.MD) 60.percent
                else 90.percent
            ),
        numColumns = numColumns(base = 1, md = 2)
    ) {
        ArticleDescription(
            active = active,
            description = article.summary,
            article = article,
        )
        ArticleDetails(
            breakpoint = breakpoint,
            active = active,
            article = article,
            animatedMargin = animatedMargin
        )
    }
}

@Composable
fun ArticleDescription(
    article: Article,
    active: Boolean,
    description: String
) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .margin(topBottom = 14.px)
            .borderRadius(20.px)
            .padding(all = 14.px)
            .backgroundColor(Colors.DimGray)
    ) {
        Column {
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .lineHeight(1.6)
                    .fontWeight(FontWeight.Normal)
                    .color(if (active) Colors.White else Colors.WhiteSmoke)
                    .toAttrs()
            ) {
                Text(description)
            }
            Link(
                modifier = Modifier
                    .margin(top= 5.px)
                    .fontFamily(FONT_FAMILY)
                    .fontStyle(FontStyle.Italic)
                    .fontSize(10.px)
                    .color(Colors.DimGray)
                    .cursor(Cursor.Pointer)
                    .backgroundColor(Colors.White)
                    .borderRadius(20.px)
                    .padding(5.px)
                    .textDecorationLine(TextDecorationLine.None),
                path = article.path
            )   {
                Text("Read more")
            }
        }
    }
}

@Composable
fun ArticleDetails(
    breakpoint: Breakpoint,
    active: Boolean,
    article: Article,
    animatedMargin: CSSSizeValue<CSSUnit.px>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .margin(left = if (breakpoint >= Breakpoint.MD) 14.px else 0.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (breakpoint >= Breakpoint.MD) {
            ArticleNumber(active = active, article = article)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .margin(left = if(breakpoint <= Breakpoint.SM) 0.px else animatedMargin)
                .transition(
                   Transition.of(
                        property = "margin",
                        duration = 500.ms,
                        delay = article.ordinal * 100.ms
                    )
                )
            ,
            verticalArrangement = Arrangement.Center
        ) {
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(20.px)
                    .fontWeight(FontWeight.Bold)
                    .color(Colors.DimGray)
                    .toAttrs()
            ) {
                Text(article.title)
            }
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .fontWeight(FontWeight.Normal)
                    .color(Colors.DimGray)
                    .toAttrs()
            ) {
                Text(article.datePublished)
            }
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .fontWeight(FontWeight.Normal)
                    .color(Colors.Black)
                    .toAttrs()
            ) {
                Text(article.author)
            }
        }
    }
}

@Composable
fun ArticleNumber(
    active: Boolean,
    article: Article
) {
    Box(
        modifier = Modifier
            .margin(right = 14.px)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.px)
                .backgroundColor(Colors.DimGray)
        )
        Box(
            modifier = Modifier
                .size(40.px)
                .border(
                    width = 3.px,
                    style = LineStyle.Solid,
                    color = Colors.DimGray
                )
                .backgroundColor(if (active) Colors.DimGray else Colors.White)
                .borderRadius(50.percent),
            contentAlignment = Alignment.Center
        ) {
            P(
                attrs = Modifier
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(16.px)
                    .fontWeight(FontWeight.Bold)
                    .color(if (active) Colors.White else Colors.DimGray)
                    .toAttrs()
            ) {
                Text(article.year)
            }
        }
    }
}