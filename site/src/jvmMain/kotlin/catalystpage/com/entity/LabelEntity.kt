package catalystpage.com.entity

import catalystpage.com.model.Labels
import catalystpage.com.model.ProductLabels
import dto.LabelDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class LabelEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LabelEntity>(Labels)

    var name by Labels.name
    var color by Labels.color
    var priority by Labels.priority
    val products by ProductEntity via ProductLabels


    fun toDTO(): LabelDTO = LabelDTO(
        id = id.value,
        name = name,
        color = color,
        priority = priority
    )
}