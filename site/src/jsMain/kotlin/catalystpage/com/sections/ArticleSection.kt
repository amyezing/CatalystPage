package catalystpage.com.sections

import androidx.compose.runtime.*
import catalystpage.com.components.ArticleCard
import catalystpage.com.components.Header
import catalystpage.com.components.SectionTitle
import catalystpage.com.model.Article
import catalystpage.com.model.Section
import catalystpage.com.util.Constants.SECTION_WIDTH
import catalystpage.com.util.ObserveViewportEntered
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px


@Composable
fun ArticleSection(onMenuClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .id(Section.Articles.id)
            .fillMaxWidth()
            .margin(bottom = 100.px)
            .maxWidth(SECTION_WIDTH.px)
            .backgroundColor(Colors.White),
        contentAlignment = Alignment.TopCenter
    ) {
        ArticleHeader(onMenuClicked = onMenuClicked)

    }
}

@Composable
fun ArticleHeader(onMenuClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(onMenuClicked = onMenuClicked)
        ArticleContent()
    }
}

@Composable
fun ArticleContent() {
    val breakpoint = rememberBreakpoint()
    var animatedMargin by remember { mutableStateOf(200.px) }

    ObserveViewportEntered(
        sectionId = Section.Articles.id,
        distanceFromTop = 500.0,
        onViewportEntered = {
            animatedMargin = 50.px
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(
                if (breakpoint >= Breakpoint.MD) 100.percent
                else 90.percent
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(
            modifier = Modifier
                .fillMaxWidth(
                    if (breakpoint >= Breakpoint.MD) 60.percent
                    else 90.percent
                )
                .margin(bottom = 10.px),
            section = Section.Articles
        )
        Article.entries.forEach { article ->
            ArticleCard(
                breakpoint = breakpoint,
                active = article == Article.First,
                article = article,
                animatedMargin = animatedMargin
            )
        }
    }
}
