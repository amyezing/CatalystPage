package catalystpage.com.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object EcoPointTransactions : IntIdTable("eco_point_transactions") {
    val user = reference("user_id", Users)
    val points = integer("points")
    val reason = varchar("reason", 255).default("recycling_scan")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
