package catalystpage.com.admin.adashboards

import admin.dto.AdminPaymentSummaryDTO
import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.fetchAdminPaymentSummary
import catalystpage.com.admin.fetcher.updatePaymentStatus
import catalystpage.com.util.Constants
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun PaymentSummary() {
    var paymentSummaries by remember { mutableStateOf<List<AdminPaymentSummaryDTO>>(emptyList()) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        paymentSummaries = fetchAdminPaymentSummary()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.px)
            .gap(24.px)
            .backgroundColor(Colors.BlanchedAlmond)
    ) {
        paymentSummaries.forEach { summary ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .maxWidth(800.px)
                    .borderRadius(12.px)
                    .boxShadow(blurRadius = 8.px, color = Colors.Gray)
                    .backgroundColor(Colors.White)
                    .padding(16.px)
                    .fontFamily("Arial")
            ) {
                P { Text("🧾 Order ID: ${summary.orderId}") }
                P { Text("👤 User: ${summary.usersName}") }
                P { Text("💰 Amount: ₱${summary.amount}") }
                P { Text("💳 Method: ${summary.paymentMethod}") }
                P { Text("📅 Date: ${summary.paymentDate}") }
                P { Text("🕒 Status Updated At: ${summary.statusUpdatedAt}") }

                P(
                    attrs = Modifier
                        .color(
                            when (summary.status.uppercase()) {
                                "APPROVED" -> Colors.Green
                                "REJECTED" -> Colors.Red
                                else -> Colors.Orange
                            }
                        )
                        .fontWeight(FontWeight.Bold)
                        .toAttrs()
                ) {
                    Text("Status: ${summary.status}")
                }

                if (!summary.proofImage.isNullOrBlank()) {
                    P(attrs = Modifier.fontWeight(FontWeight.Medium).margin(top = 8.px).toAttrs()) {
                        Text("📷 Proof of Payment:")
                    }
                    Img(
                        src =  "https://${Constants.HOST}/${summary.proofImage}",
                        attrs = Modifier
                            .margin(top = 8.px)
                            .maxWidth(100.px)
                            .borderRadius(8.px)
                            .toAttrs()
                    )
                }

                // ✅ Action buttons for pending payments
                if (summary.status.equals("Pending", ignoreCase = true)) {
                    Row(modifier = Modifier.gap(12.px).margin(top = 12.px)) {
                        Button(
                            attrs = Modifier
                                .backgroundColor(Colors.Green)
                                .color(Colors.White)
                                .borderRadius(6.px)
                                .padding(8.px)
                                .onClick {
                                scope.launch {
                                    val success = updatePaymentStatus(summary.orderId, "APPROVED")
                                    if (success) {
                                        paymentSummaries = fetchAdminPaymentSummary()
                                    }
                                }
                            }
                                .toAttrs()
                        ) {
                            Text("Approve")
                        }

                        Button(
                            attrs = Modifier
                                .backgroundColor(Colors.Red)
                                .color(Colors.White)
                                .borderRadius(6.px)
                                .padding(8.px)
                                .onClick {
                                scope.launch {
                                    val success = updatePaymentStatus(summary.orderId, "REJECTED")
                                    if (success) {
                                        paymentSummaries = fetchAdminPaymentSummary()
                                    }
                                }
                            }
                                .toAttrs()
                        ) {
                            Text("Reject")
                        }
                    }
                }
            }
        }

        if (paymentSummaries.isEmpty()) {
            P(
                attrs = Modifier
                    .fontSize(18.px)
                    .fontWeight(FontWeight.SemiBold)
                    .toAttrs()
            ) {
                Text("No payment records found.")
            }
        }
    }
}