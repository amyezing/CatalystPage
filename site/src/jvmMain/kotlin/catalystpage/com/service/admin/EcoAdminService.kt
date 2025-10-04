package catalystpage.com.service.admin

import catalystpage.com.model.Users
import catalystpage.com.model.community.CommunityProgress
import catalystpage.com.model.community.UserRecycling
import catalystpage.com.service.EcoService
import dto.community.RecyclingStatus
import model.PendingRecyclingDTO
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

object EcoAdminService {
    fun confirmRequest(requestId: Int, zoneId: Int?): Boolean = transaction {
        val row = UserRecycling.select { UserRecycling.id eq requestId }
            .singleOrNull() ?: return@transaction false

        if (row[UserRecycling.status] != RecyclingStatus.PENDING) return@transaction false

        val bottles = row[UserRecycling.bottles]
        val currentZoneId = row[UserRecycling.zoneId]
        val monthYear = row[UserRecycling.monthYear]

        // Always update zoneId if admin selected it
        val finalZoneId = zoneId ?: currentZoneId

        // Update request
        UserRecycling.update({ UserRecycling.id eq requestId }) {
            it[status] = RecyclingStatus.CONFIRMED
            it[confirmedAt] = LocalDateTime.now()
            it[this.zoneId] = finalZoneId // important: always update
        }

        // Update zone progress
        if (finalZoneId != null) {
            EcoService.recordRecycling(finalZoneId, bottles, monthYear)
        } else {
            CommunityProgress.insertIgnore {
                it[CommunityProgress.monthYear] = monthYear
                it[CommunityProgress.totalBottles] = 0
            }
            CommunityProgress.update({ CommunityProgress.monthYear eq monthYear }) {
                with(SqlExpressionBuilder) { it[totalBottles] = totalBottles + bottles }
                it[updatedAt] = LocalDateTime.now()
            }
        }

        true
    }
    fun rejectRequest(requestId: Int): Boolean = transaction {
        val updated = UserRecycling.update({ UserRecycling.id eq requestId }) {
            it[status] = RecyclingStatus.REJECTED
        }
        updated > 0
    }

    fun getPendingRequests(): List<PendingRecyclingDTO> = transaction {
        (UserRecycling innerJoin Users)
            .slice(
                UserRecycling.id,
                UserRecycling.userId,
                UserRecycling.bottles,
                UserRecycling.zoneId,
                UserRecycling.monthYear,
                UserRecycling.createdAt,
                Users.name
            )
            .select { UserRecycling.status eq RecyclingStatus.PENDING }
            .map {
                PendingRecyclingDTO(
                    id = it[UserRecycling.id].value,
                    userId = it[UserRecycling.userId],
                    userName = it[Users.name],
                    zoneId = it[UserRecycling.zoneId],
                    bottles = it[UserRecycling.bottles],
                    monthYear = it[UserRecycling.monthYear],
                    createdAt = it[UserRecycling.createdAt].toString()
                )
            }
    }




}