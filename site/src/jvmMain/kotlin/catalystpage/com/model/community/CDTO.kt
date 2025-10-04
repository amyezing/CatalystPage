package catalystpage.com.model.community

import dto.community.CommunityProgressDTO
import dto.community.ZoneCityDTO
import dto.community.ZoneDTO
import dto.community.ZoneProgressDTO
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toZoneDTO() = ZoneDTO(
    id = this[Zones.id],
    name = this[Zones.name],
    description = this[Zones.description]
)

fun ResultRow.toZoneCityDTO() = ZoneCityDTO(
    id = this[ZoneCities.id],
    zoneId = this[ZoneCities.zoneId],
    cityName = this[ZoneCities.cityName]
)

fun ResultRow.toZoneProgressDTO() = ZoneProgressDTO(
    zoneId = this[ZoneProgress.zoneId],
    monthYear = this[ZoneProgress.monthYear],
    totalBottles = this[ZoneProgress.totalBottles]
)

fun ResultRow.toCommunityProgressDTO() = CommunityProgressDTO(
    monthYear = this[CommunityProgress.monthYear],
    totalBottles = this[CommunityProgress.totalBottles]
)