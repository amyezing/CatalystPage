package catalystpage.com.service

import catalystpage.com.model.community.*
import dto.community.RecyclingStatus
import dto.community.RewardType
import model.UserProgressDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object EcoService {

    // helper to get current month-year string
    fun currentMonthYear(): String {
        val now = LocalDate.now()
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    // Map address -> zone_id by checking if any zone_cities.city_name exists in address (case-insensitive)
    fun determineZoneIdFromAddress(address: String?): Int? {
        if (address.isNullOrBlank()) return null
        return transaction {
            ZoneCities.selectAll().firstOrNull { row ->
                val city = row[ZoneCities.cityName]
                address.contains(city, ignoreCase = true)
            }?.get(ZoneCities.zoneId)
        }
    }

    fun getCommunityTotal(monthYear: String = currentMonthYear()): Int {
        return transaction {
            CommunityProgress
                .select { CommunityProgress.monthYear eq monthYear }
                .singleOrNull()
                ?.get(CommunityProgress.totalBottles)
                ?: 0
        }
    }

    fun getCommunityLifetimeTotal(): Int {
        return transaction {
            CommunityProgress
                .slice(CommunityProgress.totalBottles.sum())
                .selectAll()
                .singleOrNull()
                ?.getOrNull(CommunityProgress.totalBottles.sum()) ?: 0
        }
    }


    fun getUserProgress(userId: Int): UserProgressDTO = transaction {
        val monthYear = currentMonthYear()

        val monthlyTotal = UserRecycling
            .slice(UserRecycling.bottles.sum())
            .select {
                (UserRecycling.userId eq userId) and
                        (UserRecycling.status eq RecyclingStatus.CONFIRMED) and
                        (UserRecycling.monthYear eq monthYear)
            }
            .singleOrNull()?.getOrNull(UserRecycling.bottles.sum()) ?: 0

        val lifetimeTotal = UserRecycling
            .slice(UserRecycling.bottles.sum())
            .select {
                (UserRecycling.userId eq userId) and
                        (UserRecycling.status eq RecyclingStatus.CONFIRMED)
            }
            .singleOrNull()?.getOrNull(UserRecycling.bottles.sum()) ?: 0

        UserProgressDTO(monthlyTotal, lifetimeTotal)
    }


    // Record a recycling event
    fun recordRecycling(zoneId: Int, bottles: Int, monthYear: String = currentMonthYear()) = transaction {
        ZoneProgress.insertIgnore {
            it[this.zoneId] = zoneId
            it[this.monthYear] = monthYear
            it[this.totalBottles] = 0
        }
        ZoneProgress.update({ (ZoneProgress.zoneId eq zoneId) and (ZoneProgress.monthYear eq monthYear) }) {
            with(SqlExpressionBuilder) {
                it[totalBottles] = totalBottles + bottles
            }
            it[updatedAt] = LocalDateTime.now()
        }

        CommunityProgress.insertIgnore {
            it[this.monthYear] = monthYear
            it[this.totalBottles] = 0
        }
        CommunityProgress.update({ CommunityProgress.monthYear eq monthYear }) {
            with(SqlExpressionBuilder) {
                it[totalBottles] = totalBottles + bottles
            }
            it[updatedAt] = LocalDateTime.now()
        }
    }



    // Distribute zone rewards for month (call on scheduled job at month end)
    fun distributeZoneRewardsForMonth(monthYear: String = currentMonthYear()) {
        transaction {
            // for each zone that reached threshold
            val threshold = 500
            val qualifyingZones = ZoneProgress.select { ZoneProgress.monthYear eq monthYear and (ZoneProgress.totalBottles greaterEq threshold) }
                .map { it[ZoneProgress.zoneId] }

            qualifyingZones.forEach { zoneId ->
                // get all users in that zone this month
                // We can get users who contributed this month (from user_recycling) or all users whose zone_city_id maps to this zone.
                val contributorUserIds = (UserRecycling.select { UserRecycling.zoneId eq zoneId and (UserRecycling.monthYear eq monthYear) }
                    .map { it[UserRecycling.userId] }
                    .toSet())

                // Grant +10 eco points and log community_rewards
                contributorUserIds.forEach { uid ->
                    // add +10 points to user's eco balance (you must adapt to your user_eco_points implementation)
                    // Example pseudo:
                    // UserEcoPointsService.addPoints(uid, 10, "ZoneTopReward", "Zone ${zoneId} reached $threshold this month")

                    CommunityRewards.insert {
                        it[CommunityRewards.userId] = uid
                        it[CommunityRewards.rewardType] = RewardType.PriorityTester
                        it[CommunityRewards.rewardValue] = 10
                        it[CommunityRewards.note] = "Zone $zoneId reached $threshold bottles in $monthYear"
                        it[CommunityRewards.monthYear] = monthYear
                    }
                }
            }
        }
    }
    fun getTopZone(monthYear: String = currentMonthYear()): Pair<String, Int> = transaction {
        (ZoneProgress innerJoin Zones)
            .slice(Zones.name, ZoneProgress.totalBottles)
            .select { ZoneProgress.monthYear eq monthYear }
            .orderBy(ZoneProgress.totalBottles, SortOrder.DESC)
            .limit(1)
            .map { it[Zones.name] to it[ZoneProgress.totalBottles] }
            .firstOrNull() ?: ("N/A" to 0)
    }


    // Distribute community draw if community milestone reached
    fun distributeCommunityRewardIfQualified(monthYear: String = currentMonthYear()) {
        transaction {
            val milestone = 8000
            val cp = CommunityProgress.select { CommunityProgress.monthYear eq monthYear }.singleOrNull() ?: return@transaction
            if (cp[CommunityProgress.totalBottles] < milestone) return@transaction

            // build pool: users who contributed this month (from user_recycling)
            val contributors = UserRecycling.slice(UserRecycling.userId)
                .select { UserRecycling.monthYear eq monthYear }
                .withDistinct()
                .map { it[UserRecycling.userId] }

            if (contributors.isEmpty()) return@transaction

            // Weighted or unweighted selection: for weighted, use bottles per user
            val weights = contributors.map { uid ->
                val total = UserRecycling.select { (UserRecycling.userId eq uid) and (UserRecycling.monthYear eq monthYear) }
                    .sumOf { it[UserRecycling.bottles] } // sum by bottles (may need cast)
                uid to (total.coerceAtLeast(1))
            }.toMap()

            // pick weighted random
            val totalWeight = weights.values.sum()
            var r = Random.nextInt(totalWeight)
            val winner = weights.entries.first { (uid, w) ->
                r -= w
                r < 0
            }.key

            // award winner: +20 points + flavor royalty record
            // UserEcoPointsService.addPoints(winner, 20, "CommunityDraw", "Winner for $monthYear")
            CommunityRewards.insert {
                it[CommunityRewards.userId] = winner
                it[CommunityRewards.rewardType] = RewardType.FlavorRoyalty
                it[CommunityRewards.rewardValue] = 20
                it[CommunityRewards.note] = "Winner for community milestone $monthYear"
                it[CommunityRewards.monthYear] = monthYear
            }
        }
    }

    fun recordRecyclingRequest(userId: Int, bottles: Int, zoneId: Int?): Int = transaction {
        val monthYear = currentMonthYear()
        val id = UserRecycling.insert {
            it[this.userId] = userId
            it[this.bottles] = bottles
            it[this.zoneId] = zoneId
            it[this.monthYear] = monthYear
            it[this.status] = RecyclingStatus.PENDING
            it[this.createdAt] = LocalDateTime.now()
        } get UserRecycling.id

        id.value
    }



    fun confirmRecyclingRequest(id: Int): Boolean {
        return transaction {
            val updated = UserRecycling.update({ UserRecycling.id eq id }) {
                it[status] = RecyclingStatus.CONFIRMED
                it[confirmedAt] = LocalDateTime.now()
            }
            updated > 0
        }
    }
    fun rejectRecyclingRequest(id: Int): Boolean {
        return transaction {
            val updated = UserRecycling.update({ UserRecycling.id eq id }) {
                it[status] = RecyclingStatus.REJECTED
            }
            updated > 0
        }
    }
    fun getPendingRequests(): List<Map<String, Any?>> {
        return transaction {
            UserRecycling.select { UserRecycling.status eq RecyclingStatus.PENDING }
                .map {
                    mapOf(
                        "id" to it[UserRecycling.id],
                        "userId" to it[UserRecycling.userId],
                        "bottles" to it[UserRecycling.bottles],
                        "zoneId" to it[UserRecycling.zoneId],
                        "monthYear" to it[UserRecycling.monthYear],
                        "createdAt" to it[UserRecycling.createdAt].toString()
                    )
                }
        }
    }
}
