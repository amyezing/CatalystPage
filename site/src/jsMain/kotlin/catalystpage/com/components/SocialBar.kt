package catalystpage.com.components

import androidx.compose.runtime.Composable
import catalystpage.com.styles.SocialLinkStyle
import catalystpage.com.util.Constants.FBPAGE
import catalystpage.com.util.Constants.IGPAGE
import catalystpage.com.util.Constants.TIKTOKPG
import catalystpage.com.util.Constants.XPAGE
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.navigation.OpenLinkStrategy
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.px

@Composable
fun SocialBar(row: Boolean = false) {
    if (row) {
        Row(
            modifier = Modifier
                .margin(top = 25.px)
                .padding(leftRight = 25.px)
                .minHeight(40.px)
                .borderRadius(r = 20.px)
                .backgroundColor(Colors.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SocialLinks(row = true)
        }
    } else {
        Column(
            modifier = Modifier
                .margin(right = 25.px)
                .padding(topBottom = 25.px)
                .minWidth(40.px)
                .borderRadius(r = 20.px)
                .backgroundColor(Colors.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SocialLinks()
        }
    }
}

@Composable
private fun SocialLinks(row: Boolean = false) {
    Link(
        path = FBPAGE,
        openExternalLinksStrategy = OpenLinkStrategy.IN_NEW_TAB
    ) {
        FaFacebook(
            modifier = SocialLinkStyle.toModifier()
                .margin(
                    bottom = if (row) 0.px else 40.px,
                    right = if (row) 40.px else 0.px
                ),
            size = IconSize.LG
        )
    }
    Link(
        path = XPAGE,
        openExternalLinksStrategy = OpenLinkStrategy.IN_NEW_TAB
    ) {
        FaX(
            modifier = SocialLinkStyle.toModifier()
                .margin(
                    bottom = if (row) 0.px else 40.px,
                    right = if (row) 40.px else 0.px
                ),
            size = IconSize.LG
        )
    }
    Link(
        path = IGPAGE,
        openExternalLinksStrategy = OpenLinkStrategy.IN_NEW_TAB
    ) {
        FaInstagram(
            modifier = SocialLinkStyle.toModifier()
                .margin(
                    bottom = if (row) 0.px else 40.px,
                    right = if (row) 40.px else 0.px
                ),
            size = IconSize.LG
        )
    }
    Link(
        path = TIKTOKPG,
        openExternalLinksStrategy = OpenLinkStrategy.IN_NEW_TAB
    ) {
        FaTiktok(
            modifier = SocialLinkStyle.toModifier(),
            size = IconSize.LG
        )
    }
}