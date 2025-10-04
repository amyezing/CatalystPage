package catalystpage.com.service

import catalystpage.com.model.EcoPointTransactions
import catalystpage.com.model.UserEcoPoints
import catalystpage.com.model.Users
import dto.EcoPointTransactionDTO
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime


object EcoPointService {

    // Add points by user ID
    fun addPointsByUserId(userId: Int, pointsToAdd: Int, reason: String) = transaction {
        // Ensure row exists
        UserEcoPoints.insertIgnore {
            it[this.userId] = userId
            it[this.points] = 0
            it[this.updatedAt] = LocalDateTime.now()
        }

        // Update running total
        UserEcoPoints.update({ UserEcoPoints.userId eq userId }) {
            with(SqlExpressionBuilder) {
                it[points] = points + pointsToAdd
            }
            it[updatedAt] = LocalDateTime.now()
        }

        // Log transaction
        EcoPointTransactions.insert {
            it[this.user] = EntityID(userId, Users)
            it[this.points] = pointsToAdd
            it[this.reason] = reason
            it[this.createdAt] = LocalDateTime.now()
        }
    }

    fun getTotalPoints(userId: Int): Int = transaction {
        EcoPointTransactions
            .slice(EcoPointTransactions.points.sum())
            .select { EcoPointTransactions.user eq userId }
            .firstOrNull()?.getOrNull(EcoPointTransactions.points.sum()) ?: 0
    }

    fun getTransactions(userId: Int): List<EcoPointTransactionDTO> = transaction {
        EcoPointTransactions
            .select { EcoPointTransactions.user eq userId }
            .orderBy(EcoPointTransactions.createdAt, SortOrder.DESC)
            .map { row ->
                EcoPointTransactionDTO(
                    id = row[EcoPointTransactions.id].value,
                    userId = row[EcoPointTransactions.user].value,
                    points = row[EcoPointTransactions.points],
                    reason = row[EcoPointTransactions.reason],
                    createdAt = row[EcoPointTransactions.createdAt].toString()
                )
            }
    }


}