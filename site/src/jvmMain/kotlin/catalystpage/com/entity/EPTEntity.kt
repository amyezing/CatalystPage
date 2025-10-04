package catalystpage.com.entity

import catalystpage.com.model.EcoPointTransactions
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class EcoPointTransactionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EcoPointTransactionEntity>(EcoPointTransactions)

    var user by UserEntity referencedOn EcoPointTransactions.user
    var points by EcoPointTransactions.points
    var reason by EcoPointTransactions.reason
    var createdAt by EcoPointTransactions.createdAt
}

