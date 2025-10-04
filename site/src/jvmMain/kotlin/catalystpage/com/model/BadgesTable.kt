package catalystpage.com.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.datetime

object Badges : Table("badges") {
    val id = varchar("id", 50) // e.g. "starter", "collector"
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val iconUrl = text("icon_url").nullable()
    val reward = varchar("reward", 255).nullable()
    val ecoPoints = integer("eco_points").default(0)

    override val primaryKey = PrimaryKey(id)
}

// user_badges table
object UserBadges : Table("user_badges") {
    val userId = integer("user_id").references(Users.id)
    val badgeId = varchar("badge_id", 50).references(Badges.id)
    val unlocked = bool("unlocked").default(false)
    val unlockedAt = datetime("unlocked_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(userId, badgeId)
}