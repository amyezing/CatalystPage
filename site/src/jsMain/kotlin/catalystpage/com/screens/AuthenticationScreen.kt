package catalystpage.com.screens

import androidx.compose.runtime.*
import catalystpage.com.database.LocalUserState
import catalystpage.com.database.logOutUser
import catalystpage.com.database.signInWithGoogle
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text


@Composable
fun AuthScreen() {
    val userState = LocalUserState.current
    val breakpoint = rememberBreakpoint()

    // Redirect to dashboard automatically when userState changes
    LaunchedEffect(userState.value) {
        if (userState.value != null) {
            window.location.href = "/dashboard"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(if (breakpoint <= Breakpoint.MD) 100.percent else 50.percent)
            .padding(16.px)
            .borderRadius(10.px),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.px)
    ) {
        P(
            attrs = Modifier
                .fontSize(30.px)
                .fontFamily(FONT_FAMILY)
                .fontWeight(FontWeight.Bold)
                .fontStyle(FontStyle.Italic)
                .color(Colors.DimGray)
                .textAlign(TextAlign.Center)
                .toAttrs()
        ) {
            Text("Welcome to Catalyst")
        }

        if (userState.value == null) {
            Button(
                attrs = Modifier
                    .border(style = LineStyle.None)
                    .backgroundColor(rgba(175, 238, 238, 0.6))
                    .color(Colors.DimGray)
                    .fontWeight(FontWeight.Medium)
                    .borderRadius(5.px)
                    .padding(10.px)
                    .fontFamily(FONT_FAMILY)
                    .onClick {
                        signInWithGoogle { syncedUser ->
                            // Update global state after successful sign-in
                            userState.value = syncedUser
                        }
                    }
                    .toAttrs()
            ) {
                Text("Sign in with Google")
            }
        } else {
            Text("✅ Signed in as ${userState.value!!.email}")

            Button(
                attrs = Modifier
                    .backgroundColor(rgba(0, 0, 0, 0.6))
                    .color(Colors.White)
                    .borderRadius(5.px)
                    .padding(10.px)
                    .onClick {
                        logOutUser()
                        userState.value = null
                    }
                    .toAttrs()
            ) {
                Text("Logout")
            }
        }
    }
}
