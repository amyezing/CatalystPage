package catalystpage.com.service

import catalystpage.com.model.Badges
import catalystpage.com.model.EcoPointTransactions
import catalystpage.com.model.UserBadges
import catalystpage.com.model.UserEcoPoints
import dto.EcoPointTransactionDTO
import dto.UserEcoPointsDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction

class UserEcoPointsService {

    fun getPoints(userId: Int): UserEcoPointsDTO? = transaction {
        UserEcoPoints.select { UserEcoPoints.userId eq userId }
            .map { row ->
                UserEcoPointsDTO(
                    userId = row[UserEcoPoints.userId],
                    points = row[UserEcoPoints.points],
                    updatedAt = row[UserEcoPoints.updatedAt].toString()
                )
            }.singleOrNull()
    }

    fun addPoints(userId: Int, additionalPoints: Int) = transaction {
        val existing = UserEcoPoints.select { UserEcoPoints.userId eq userId }.singleOrNull()
        if (existing != null) {
            UserEcoPoints.update({ UserEcoPoints.userId eq userId }) {
                with(SqlExpressionBuilder) {
                    it[points] = points + additionalPoints
                    it[updatedAt] = CurrentDateTime
                }
            }
        } else {
            UserEcoPoints.insert {
                it[UserEcoPoints.userId] = userId
                it[points] = additionalPoints
            }
        }

        // Check badge unlocks
        unlockBadgesIfEligible(userId)
    }

    private fun unlockBadgesIfEligible(userId: Int) = transaction {
        val userPoints = getPoints(userId)?.points ?: 0
        val unlockedBadges = UserBadges.select { UserBadges.userId eq userId }
            .map { it[UserBadges.badgeId] }

        // Fetch badges that have eco_points thresholds
        Badges.selectAll().forEach { badge ->
            val badgeId = badge[Badges.id]
            val requiredPoints = badge[Badges.ecoPoints]

            if (userPoints >= requiredPoints && !unlockedBadges.contains(badgeId)) {
                UserBadges.insert {
                    it[UserBadges.userId] = userId
                    it[UserBadges.badgeId] = badgeId
                    it[UserBadges.unlocked] = true
                    it[UserBadges.unlockedAt] = CurrentDateTime
                }
            }
        }
    }

}