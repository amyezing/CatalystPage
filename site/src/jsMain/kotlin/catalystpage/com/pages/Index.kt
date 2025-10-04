package catalystpage.com.pages

import androidx.compose.runtime.*
import catalystpage.com.components.BackToTopButton
import catalystpage.com.components.OverflowMenu
import catalystpage.com.sections.*
import catalystpage.com.wrapper.CartViewModel
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page

@Page
@Composable
fun HomePage() {
    var menuOpened by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainSection(onMenuClicked = { menuOpened = true })
            AboutSection()
            ProductSection()
            ContactSection()
            FooterSection()
        }
        BackToTopButton()
        if (menuOpened) {
            OverflowMenu(onMenuClosed = { menuOpened = false })
        }
    }
}

@Page(routeOverride = "/articles")
@Composable
fun ArticlePage() {
    var menuOpened by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArticleSection(onMenuClicked = { menuOpened = true })
            FooterSection()
        }
        if (menuOpened) {
            OverflowMenu(onMenuClosed = { menuOpened = false })
        }
    }
}

@Page(routeOverride = "/signIn")
@Composable
fun AuthenticationPage() {
    var menuOpened by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthSection(onMenuClicked =  { menuOpened = true })
        }
        if (menuOpened) {
            OverflowMenu(onMenuClosed = { menuOpened = false })
        }
    }
}


@Page(routeOverride = "/dashboard")
@Composable
fun DashboardPage() {
    var menuOpened by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
                .backgroundColor(Colors.WhiteSmoke),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DashboardSection(
                onMenuClicked = { menuOpened = true })
        }
        if (menuOpened) {
            OverflowMenu(onMenuClosed =  { menuOpened = false })
        }
    }
}


