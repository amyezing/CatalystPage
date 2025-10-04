package catalystpage.com.components

import androidx.compose.runtime.*
import catalystpage.com.database.EcoFetcher
import catalystpage.com.util.Constants.FONT_FAMILY
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import kotlinx.coroutines.launch
import model.UserProgressResponse
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement
@Composable
fun RecyclingProgressCard(
    userId: Int,
    zoneId: Int,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    // Local UI state
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var recycleCount by remember { mutableStateOf(1) }

    // Load total bottles (all users)
    val communityTotal by produceState(0) {
        value = EcoFetcher.getCommunityTotal()
    }

    // Load user bottles (you’ll need a backend route + EcoFetcher.getUserTotal(userId))
    val userTotal by produceState(0) {
        value = EcoFetcher.getUserTotal(userId) // <-- you create this API hook
    }

    val userProgress by produceState<UserProgressResponse?>(null) {
        value = EcoFetcher.getUserProgress(userId)
    }

    val communityLifetime by produceState(0) {
        value = EcoFetcher.getCommunityLifetime()
    }

    val topZone by produceState("" to 0) { value = EcoFetcher.getTopZone() }

    Column(
        modifier = modifier
            .borderRadius(16.px)
            .backgroundColor(rgba(255, 255, 255, 0.4))
            .padding(16.px)
            .margin(bottom = 25.px)
            .overflow(Overflow.Auto)
            .width(380.px),
        verticalArrangement = Arrangement.spacedBy(16.px)
    ) {
        // Card title
        P(
            attrs = Modifier
                .fontWeight(FontWeight.Bold)
                .fontSize(20.px)
                .color(Colors.Black)
                .toAttrs()
        ) { Text("Recycling Progress") }

        // ProgressBar function
        @Composable
        fun ProgressBar(label: String, value: Int, maxValue: Int, color: CSSColorValue) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(topBottom = 4.px),
                verticalArrangement = Arrangement.spacedBy(6.px)
            ) {
                P(
                    attrs = Modifier
                        .fontSize(14.px)
                        .fontFamily(FONT_FAMILY)
                        .fontWeight(FontWeight.Medium)
                        .color(Colors.Black)
                        .whiteSpace(WhiteSpace.Normal)
                        .styleModifier { property("overflow-wrap", "break-word") }
                        .toAttrs()
                ) { Text("$label: $value") }

                Box(
                    modifier = Modifier
                        .height(12.px)
                        .fillMaxWidth()
                        .borderRadius(6.px)
                        .backgroundColor(rgba(128, 128, 128, 0.3))
                ) {
                    val fillPercent = ((value.toFloat() / maxValue) * 100).coerceIn(0f, 100f)
                    Box(
                        modifier = Modifier
                            .height(12.px)
                            .width(fillPercent.percent)
                            .borderRadius(6.px)
                            .backgroundColor(color)
                    )
                }
            }
        }

        // Progress bars (using API values)
        ProgressBar(
            "Community Total Bottles Collected",
            communityLifetime,
            communityLifetime.coerceAtLeast(8000),
            rgba(175, 238, 238, 1f)
        )
        ProgressBar("Bottles Collected This Month", communityTotal, communityTotal.coerceAtLeast(8000), rgba(175, 238, 238, 1f))
        ProgressBar("Most Collected This Month ${topZone.first}", topZone.second, communityTotal.coerceAtLeast(8000), rgba(175, 238, 238, 1f))
        ProgressBar("Your Bottles This Month", userTotal, communityTotal, rgba(175, 238, 238, 1f))

        ProgressBar("Your Lifetime Bottles",
            userProgress?.lifetimeTotal ?: 0,
            (userProgress?.lifetimeTotal ?: 0).coerceAtLeast(100),
            rgba(64, 224, 208, 1f)
        )
        // Input + Submit Row
        P(
            attrs = Modifier
                .margin(bottom = 2.px)
                .padding(4.px)
                .fontFamily(FONT_FAMILY)
                .fontWeight(FontWeight.Medium)
                .fontSize(14.px)
                .toAttrs()
        ) { Text("Bottles to recycle") }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.px)
        ) {
            Input(
                type = InputType.Number,
                attrs = Modifier
                    .width(100.px)
                    .padding(4.px)
                    .borderRadius(8.px)
                    .toAttrs {
                        value(recycleCount.toString())
                        onInput { event ->
                            val strValue = (event.target as? HTMLInputElement)?.value ?: ""
                            // allow empty string
                            recycleCount = strValue.toIntOrNull() ?: 0
                        }
                        min("0")
                    }
            )

            Button(
                attrs = Modifier
                    .width(100.px)
                    .padding(8.px)
                    .border(style = LineStyle.None)
                    .backgroundColor(rgba(0, 0, 0, 0.6))
                    .color(Colors.White)
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .borderRadius(r = 10.px)
                    .onClick {
                        scope.launch {
                            val success = EcoFetcher.postRecycle(userId, recycleCount)
                            if (success) {
                                console.log("Recycle request submitted. Waiting for admin confirmation.")
                                toastMessage = "Recycle request submitted (pending admin approval)."
                                recycleCount = 0 // reset after submit
                            } else {
                                console.error("Failed to recycle")
                                toastMessage = "Failed to send recycle request."
                            }
                        }
                    }
                    .toAttrs()
            ) { Text("Submit") }
        }

        ToastMessage(toastMessage)
    }
}



