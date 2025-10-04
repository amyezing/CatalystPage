package catalystpage.com.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object UserEcoPoints : Table("user_eco_points") {
    val userId = integer("user_id").references(Users.id) // foreign key
    val points = integer("points").default(0)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(userId) // define primary key here
}