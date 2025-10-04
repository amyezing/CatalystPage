package catalystpage.com.admin.adashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.AdminEcoPointsApi
import catalystpage.com.components.admin.AdminQRCodeScanner
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import dto.EcoPointTransactionDTO
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLInputElement

@Composable
fun EcoPoints() {
    var scannedUserId by remember { mutableStateOf<String?>(null) }
    var transactions by remember { mutableStateOf<List<EcoPointTransactionDTO>>(emptyList()) }
    var pointsToAdd by remember { mutableStateOf(10) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(16.px)
            .width(100.percent)
    ) {
        H1 { Text("Eco Points Admin Dashboard") }

        Div(attrs = Modifier.padding(16.px).toAttrs()) {
            Text("Scan User QR Code:")
            AdminQRCodeScanner { decodedText ->
                scannedUserId = decodedText
                scope.launch {
                    transactions = AdminEcoPointsApi.getUserTransactions(decodedText.toInt())
                }
            }
        }

        scannedUserId?.let { userId ->
            H2 { Text("User ID: $userId") }

            Table {
                Thead {
                    Tr {
                        Th { Text("ID") }
                        Th { Text("Points") }
                        Th { Text("Reason") }
                        Th { Text("Created At") }
                    }
                }
                Tbody {
                    transactions.forEach { tx ->
                        Tr {
                            Td { Text(tx.id.toString()) }
                            Td { Text(tx.points.toString()) }
                            Td { Text(tx.reason) }
                            Td { tx.createdAt?.let { Text(it) } }
                        }
                    }
                }
            }

            Div(attrs = Modifier.margin(top = 16.px).toAttrs()) {
                Input(
                    type = InputType.Number,
                    attrs = {
                        style { width(80.px) }
                        onInput { event ->
                            val value = (event.target as? HTMLInputElement)?.value ?: "0"
                            pointsToAdd = value.toIntOrNull() ?: 10
                        }
                    }
                )
                Button(
                    attrs = Modifier.onClick {
                        scope.launch {
                            scannedUserId?.toIntOrNull()?.let { userId ->
                                AdminEcoPointsApi.addPoints(userId, pointsToAdd)
                                transactions = AdminEcoPointsApi.getUserTransactions(userId)
                            }
                        }
                    }.toAttrs()
                ) {
                    Text("Add Points")
                }
            }
        }
    }
}