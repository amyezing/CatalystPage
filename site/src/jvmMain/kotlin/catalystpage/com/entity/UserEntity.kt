package catalystpage.com.entity

import catalystpage.com.model.Users
import dto.UserDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    var firebaseUid by Users.firebaseUid
    var email by Users.email
    var name by Users.name
    var phone by Users.phone
    var role by Users.role
    var createdAt by Users.createdAt

    fun toDTO(): UserDTO = UserDTO(
        id = this.id.value,
        firebaseUid = this.firebaseUid,
        email = this.email,
        name = this.name,
        phone = this.phone,
        roleRaw = this.role.name,
        createdAt = this.createdAt.toString()

    )

}


