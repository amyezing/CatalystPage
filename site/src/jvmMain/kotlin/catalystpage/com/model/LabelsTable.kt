package catalystpage.com.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Labels : IntIdTable("labels") {
    val name = varchar("name", 50).uniqueIndex()
    val color = varchar("color", 20).nullable()
    val priority = integer("priority").default(0)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object ProductLabels : Table("product_labels") {
    val product = reference("product_id", Products)
    val label = reference("label_id", Labels)
    override val primaryKey = PrimaryKey(product, label)
}

//object BundleLabels : Table("bundle_labels") {
//    val bundle = reference("bundle_id", Bundles)
//    val label = reference("label_id", Labels)
//    override val primaryKey = PrimaryKey(bundle, label)
//}