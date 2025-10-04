package catalystpage.com.model

import catalystpage.com.entity.UserEntity
import dto.Role
import dto.UserDTO
import java.time.Instant

fun UserDTO.toEntity(): UserEntity = UserEntity.new {
    firebaseUid = this@toEntity.firebaseUid
    email = this@toEntity.email
    name = this@toEntity.name
    phone = this@toEntity.phone
    role = this@toEntity.role ?: Role.USER
    createdAt = this@toEntity.createdAt?.let { Instant.parse(it) } ?: Instant.now()
}


