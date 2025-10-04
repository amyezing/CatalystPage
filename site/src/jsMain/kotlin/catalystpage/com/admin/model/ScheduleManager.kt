package catalystpage.com.admin.model

import admin.dto.RecyclingScheduleDTO
import admin.dto.ScheduleType
import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.EcoAdminFetcher
import catalystpage.com.admin.fetcher.ScheduleFetcher
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import dto.community.ZoneDTO
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Composable
fun AdminScheduleManager() {
    val scope = rememberCoroutineScope()
    var schedules by remember { mutableStateOf<List<RecyclingScheduleDTO>>(emptyList()) }
    var newDate by remember { mutableStateOf("") }
    var newZoneId by remember { mutableStateOf(1) }
    var newType by remember { mutableStateOf(ScheduleType.RECYCLING) }
    var zones by remember { mutableStateOf<List<ZoneDTO>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch { schedules = ScheduleFetcher.fetchAll() }
        zones = EcoAdminFetcher.getZones()
    }

    Column(Modifier.padding(20.px)) {
        H3 { Text("Manage Recycling Schedules") }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.px)
        ) {
            // Date input
            Input(type = InputType.Date) {
                onInput { event -> newDate = event.value }
            }
            P { Text("Selected Date: $newDate") }

            // Schedule type selector
            Select(
                attrs = {
                    onInput { event ->
                        event.value?.let { value ->
                            newType = ScheduleType.valueOf(value)
                        }
                    }
                }
            ) {
                ScheduleType.entries.forEach { type ->
                    Option(value = type.name) {
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
            Select(attrs = {
                onInput { event ->
                    newZoneId = event.value?.toIntOrNull() ?: 1
                }
            }) {
                zones.forEach { zone ->
                    Option(value = zone.id.toString()) { Text(zone.name) }
                }
            }

            // Add schedule button
            Button(
                attrs = Modifier.onClick {
                    scope.launch {
                        val added = ScheduleFetcher.add(
                            RecyclingScheduleDTO(
                                zoneId = newZoneId,
                                scheduleDate = newDate,
                                type = newType
                            )
                        )
                        schedules = schedules + added
                    }
                }.toAttrs()
            ) {
                Text("Add Schedule")
            }
        }




        // Existing schedules
        schedules.forEach { s ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.px),
                modifier = Modifier.padding(topBottom = 4.px)
            ) {
                Text("${s.scheduleDate} (Zone ${s.zoneId}) - ${s.type.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }}")
                Button(
                    attrs = Modifier.onClick {
                        scope.launch {
                            ScheduleFetcher.delete(s.id!!)
                            schedules = schedules.filterNot { it.id == s.id }
                        }
                    }.toAttrs()
                ) {
                    Text("Delete")
                }
            }
        }
    }
}