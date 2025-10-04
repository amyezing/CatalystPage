package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.styles.ProductSectionStyle
import catalystpage.com.util.Constants.FONT_FAMILY
import catalystpage.com.util.Res
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.navigation.OpenLinkStrategy
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.compose.css.Width
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.argb
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import dto.ProductDTO
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: ProductDTO,
    link: String // You had `link` used but not passed, so add it here
) {
    Link(
        modifier = ProductSectionStyle.toModifier()
            .textDecorationLine(TextDecorationLine.None),
        path = link,
        openExternalLinksStrategy = OpenLinkStrategy.IN_PLACE
    ) {
        Column(
            modifier = modifier
                .id("columnParent")
                .width(Width.MaxContent)
        ) {
            Box(
                modifier = Modifier
                    .id("boxParent")
                    .fillMaxWidth()
                    .maxWidth(300.px)
                    .margin(bottom = 20.px)
            ) {
                Image(
                    modifier = Modifier
                        .size(300.px)
                        .objectFit(ObjectFit.Cover),
                    src = product.imageUrl ?: "", // safe call
                    alt = product.name
                )
                Box(
                    modifier = Modifier
                        .id("greenOverlay")
                        .fillMaxHeight()
                        .backgroundColor(argb(a = 0.5f, r = 41, g = 41, b = 41)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .id("linkIcon")
                            .size(32.px),
                        src = Res.Icon.link,
                        alt = "Link Icon"
                    )
                }
            }
            P(
                attrs = Modifier
                    .id("portfolioTitle")
                    .fillMaxWidth()
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .color(Color.dimgray)
                    .fontWeight(FontWeight.Bold)
                    .toAttrs()
            ) {
                Text(product.name)
            }
            P(
                attrs = Modifier
                    .id("portfolioDesc")
                    .width(200.px)
                    .margin(topBottom = 0.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(15.px)
                    .fontWeight(FontWeight.Normal)
                    .color(Color.dimgray)
                    .opacity(70.percent)
                    .toAttrs()
            ) {
                Text(product.description ?: "")
            }
        }
    }
}