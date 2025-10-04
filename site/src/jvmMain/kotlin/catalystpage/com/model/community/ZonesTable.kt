package catalystpage.com.model.community

import catalystpage.com.model.Users
import dto.community.RecyclingStatus
import dto.community.RewardType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Zones : Table("zones") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
}

object ZoneCities : Table("zone_cities") {
    val id = integer("id").autoIncrement()
    val zoneId = integer("zone_id").references(Zones.id)
    val cityName = varchar("city_name", 100)
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
}

object ZoneProgress : Table("zone_progress") {
    val id = integer("id").autoIncrement()
    val zoneId = integer("zone_id").references(Zones.id)
    val monthYear = varchar("month_year", 7) // "YYYY-MM"
    val totalBottles = integer("total_bottles").default(0)
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { java.time.LocalDateTime.now() }
}

object CommunityProgress : Table("community_progress") {
    val id = integer("id").autoIncrement()
    val monthYear = varchar("month_year", 7).uniqueIndex()
    val totalBottles = integer("total_bottles").default(0)
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { java.time.LocalDateTime.now() }
}

object CommunityRewards : Table("community_rewards") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val rewardType = enumerationByName("reward_type", 32, RewardType::class)
    val rewardValue = integer("reward_value").nullable()
    val note = varchar("note", 255).nullable()
    val monthYear = varchar("month_year", 7)
    val createdAt = datetime("created_at").clientDefault { java.time.LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

object UserRecycling : IntIdTable("user_recycling") {
    val userId = integer("user_id").references(Users.id)
    val bottles = integer("bottles")
    val zoneId = integer("zone_id").references(Zones.id).nullable()
    val monthYear = varchar("month_year", 7)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val status = enumerationByName("status", 10, RecyclingStatus::class)
        .default(RecyclingStatus.PENDING)
    val confirmedAt = datetime("confirmed_at").nullable()
}

