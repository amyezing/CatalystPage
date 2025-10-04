package catalystpage.com.service

import catalystpage.com.model.Badges
import catalystpage.com.model.UserBadges
import catalystpage.com.model.UserEcoPoints
import catalystpage.com.model.Users
import dto.BadgeDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class BadgeService {

    private fun getUserIdByFirebaseUid(firebaseUid: String): Int? = transaction {
        Users
            .slice(Users.id)
            .select { Users.firebaseUid eq firebaseUid }
            .map { it[Users.id].value }
            .singleOrNull()
    }

    fun getAllBadges(): List<BadgeDTO> = transaction {
        Badges.selectAll().map { row ->
            BadgeDTO(
                id = row[Badges.id],
                name = row[Badges.name],
                description = row[Badges.description] ?: "",
                iconUrl = row[Badges.iconUrl] ?: "",
                reward = row[Badges.reward] ?: "",
                ecoPoints = row[Badges.ecoPoints]
            )
        }
    }

    fun getUserBadges(firebaseUid: String): List<BadgeDTO> = transaction {
        val userId = getUserIdByFirebaseUid(firebaseUid) ?: return@transaction emptyList()

        val unlockedBadges = UserBadges
            .select { UserBadges.userId eq userId }
            .associateBy({ it[UserBadges.badgeId] }, { it })

        Badges.selectAll().map { row ->
            val userBadge = unlockedBadges[row[Badges.id]]
            BadgeDTO(
                id = row[Badges.id],
                name = row[Badges.name],
                description = row[Badges.description] ?: "",
                iconUrl = row[Badges.iconUrl] ?: "",
                reward = row[Badges.reward] ?: "",
                ecoPoints = row[Badges.ecoPoints],
                unlocked = userBadge?.get(UserBadges.unlocked) ?: false,
                unlockedAt = userBadge?.get(UserBadges.unlockedAt)?.toString()
            )
        }
    }

    fun unlockBadge(firebaseUid: String, badgeId: String): Boolean = transaction {
        val userId = getUserIdByFirebaseUid(firebaseUid) ?: return@transaction false

        // 1) Try to update existing row
        val updated = UserBadges.update({
            (UserBadges.userId eq userId) and (UserBadges.badgeId eq badgeId)
        }) {
            it[unlocked] = true
        }

        // 2) If no row existed, insert a new one
        if (updated == 0) {
            UserBadges.insert { stmt ->
                stmt[UserBadges.userId] = userId
                stmt[UserBadges.badgeId] = badgeId
                stmt[UserBadges.unlocked] = true
            }
        }

        true
    }
    fun unlockBadgesByPoints(firebaseUid: String) = transaction {
        val userId = getUserIdByFirebaseUid(firebaseUid) ?: return@transaction

        val userPoints = UserEcoPoints
            .select { UserEcoPoints.userId eq userId }
            .singleOrNull()?.get(UserEcoPoints.points) ?: 0

        val eligibleBadges = Badges
            .select { Badges.ecoPoints lessEq userPoints }
            .map { it[Badges.id] }

        eligibleBadges.forEach { badgeId ->
            val exists = UserBadges.select {
                (UserBadges.userId eq userId) and (UserBadges.badgeId eq badgeId)
            }.any()

            if (!exists) {
                UserBadges.insert { stmt ->
                    stmt[UserBadges.userId] = userId
                    stmt[UserBadges.badgeId] = badgeId
                    stmt[UserBadges.unlocked] = true
                }
            }
        }
    }

}