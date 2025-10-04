package catalystpage.com.admin.adashboards

import admin.dto.AuditLogDTO
import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.AuditFetcher
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Composable
fun AuditLogs() {
    var logs by remember { mutableStateOf<List<AuditLogDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            logs = AuditFetcher.getAll()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.px)) {
        if (isLoading) {
            P { Text("Loading audit logs...") }
        } else if (error != null) {
            P { Text("Error: $error") }
        } else {
            Table(
                attrs = Modifier
                    .fillMaxWidth()
                    .border(1.px, color = Colors.Grey).toAttrs()
            ) {
                Thead {
                    Tr {
                        listOf("ID", "Entity", "Action", "Description", "Performed By", "Created At").forEach { header ->
                            Th(
                                attrs = Modifier
                                    .padding(8.px)
                                    .width(120.px)
                                    .toAttrs()
                            ) { Text(header) }
                        }
                    }
                }
                Tbody {
                    logs.forEach { log ->
                        Tr {
                            listOf(
                                log.id.toString(),
                                log.entity,
                                log.action,
                                log.description ?: "-",
                                log.createdBy?.toString() ?: "-",
                                log.createdAt ?: "-"
                            ).forEach { value ->
                                Td(
                                    attrs = Modifier.padding(8.px).toAttrs()
                                ) { Text(value) }
                            }
                        }
                    }
                }
            }
        }
    }
}