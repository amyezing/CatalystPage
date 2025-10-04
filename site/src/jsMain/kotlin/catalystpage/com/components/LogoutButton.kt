package catalystpage.com.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import catalystpage.com.database.logOutUser
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun LogoutButton() {
    val scope = rememberCoroutineScope()
    val pageContext = rememberPageContext()

    Button(
        attrs = Modifier
            .padding(5.px)
            .color(Colors.Black)
            .border(
                style = LineStyle.Solid,
                color = Colors.PaleTurquoise
            )
            .backgroundColor(Colors.White)
            .borderRadius(r = 15.px)
            .fontFamily(FONT_FAMILY)
            .onClick {
                scope.launch {
                    logOutUser()
                    localStorage.removeItem("user")
                    pageContext.router.navigateTo("/signIn")
                }
            }
            .toAttrs()
    ) {
        Text("Logout")
    }
}