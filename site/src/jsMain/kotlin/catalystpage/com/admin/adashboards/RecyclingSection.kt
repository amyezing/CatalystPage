package catalystpage.com.admin.adashboards

import admin.dto.RecyclingScheduleDTO
import androidx.compose.runtime.*
import catalystpage.com.admin.AdminDashboard
import catalystpage.com.admin.fetcher.EcoAdminFetcher
import catalystpage.com.admin.fetcher.ScheduleFetcher
import catalystpage.com.admin.model.AdminScheduleManager
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.toAttrs
import dto.community.ZoneDTO
import kotlinx.coroutines.launch
import model.PendingRecyclingDTO
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.*

@Composable
fun RecyclingSection() {
    var requests by remember { mutableStateOf<List<PendingRecyclingDTO>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var zones by remember { mutableStateOf<List<ZoneDTO>>(emptyList()) }
    val selectedZones = remember { mutableStateMapOf<Int, Int?>() }
    var schedules by remember { mutableStateOf<List<RecyclingScheduleDTO>>(emptyList()) }


    // Load data once
    LaunchedEffect(Unit) {
        requests = EcoAdminFetcher.getPendingRequests()
        zones = EcoAdminFetcher.getZones()
        requests.forEach { selectedZones[it.id] = it.zoneId }
        schedules = ScheduleFetcher.fetchAll()
    }

    Column {
        H2 { Text("♻️ Pending Recycling Requests") }

        Table(attrs = Modifier.fillMaxWidth().toAttrs()) {
            Thead {
                Tr {
                    listOf("ID", "User ID", "Zone", "Bottles", "Month", "Action").forEach {
                        Th { Text(it) }
                    }
                }
            }
            Tbody {
                requests.forEach { req ->
                    Tr {
                        Td { Text(req.id.toString()) }
                        Td { Text(req.userName ?: "N/A") }
                        Td {
                            Select(attrs = {
                                onChange { event ->
                                    val value = event.value
                                    selectedZones[req.id] = value?.takeIf { it.isNotBlank() }?.toIntOrNull()

                                }
                            }) {
                                Option(value = "") { Text("Select a zone") }
                                zones.forEach { zone ->
                                    Option(value = zone.id.toString()) { Text(zone.name) }
                                }
                            }
                        }
                        Td { Text(req.bottles.toString()) }
                        Td { Text(req.monthYear ?: "") }
                        Td {
                            Row {

                                Button(
                                    attrs = Modifier.onClick {
                                        scope.launch {
                                            val zoneToSend = selectedZones[req.id] ?: req.zoneId
                                            val success = EcoAdminFetcher.confirmRequest(id = req.id, zoneId = zoneToSend)
                                            if (success) {
                                                requests = EcoAdminFetcher.getPendingRequests()
                                            }
                                        }
                                    }.toAttrs()
                                ) { Text("Confirm") }
                                Button(
                                    attrs = Modifier
                                        .backgroundColor((rgba(r = 0, g = 0, b = 0, 0.6)))
                                        .color(Colors.White)
                                        .onClick {
                                            scope.launch {
                                                val success = EcoAdminFetcher.rejectRequest(req.id)
                                                if (success) {
                                                    requests = EcoAdminFetcher.getPendingRequests()
                                                }
                                            }
                                        }
                                        .toAttrs()
                                )   {
                                    Text("Reject")
                                }

                            }
                        }
                    }
                }
            }
        }
    }

}


