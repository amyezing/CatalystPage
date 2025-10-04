package catalystpage.com.service

import catalystpage.com.db.EnvConfig
import catalystpage.com.entity.UserEntity
import catalystpage.com.model.UserEcoPoints
import catalystpage.com.model.Users
import dto.Role
import dto.UpdateUserDTO
import dto.UserDTO
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object UserService {

    private val adminEmails = EnvConfig.adminEmails
    fun findOrCreate(firebaseUid: String, email: String): UserEntity {
        return transaction {
            val cleanedUid = firebaseUid.trim()

            val existing = UserEntity.find { Users.firebaseUid eq cleanedUid }.firstOrNull()
            if (existing != null) return@transaction existing

            val role = if (email.trim().lowercase() in adminEmails.map { it.lowercase() }) Role.ADMIN else Role.USER

            val newUser = UserEntity.new {
                this.firebaseUid = cleanedUid
                this.email = email
                this.role = role
                this.createdAt = Instant.now()
            }

            // ✅ Send welcome email
            EmailService.sendMail(
                to = email,
                subject = "Welcome to Catalyst!",
                body = """
                Hi there,
                
                Welcome to Catalyst! Your account has been successfully created.
                
                Thank you for joining us.
                
                — Catalyst Team
            """.trimIndent()
            )

            newUser
        }
    }
    fun getById(userId: Int): UserDTO? = transaction {
        UserEntity.findById(userId)?.let { user ->
            UserDTO(
                id = user.id.value,
                firebaseUid = user.firebaseUid,
                email = user.email,
                name = user.name,
                phone = user.phone,
                roleRaw = user.role.name,
                createdAt = user.createdAt.toString()
            )
        }
    }


    fun getByFirebaseUid(uid: String): UserEntity? = transaction {
        UserEntity.find { Users.firebaseUid eq uid.trim() }.singleOrNull()
    }

    fun getAll(): List<UserEntity> = transaction {
        UserEntity.all().toList()
    }

    fun updateUserRole(
        firebaseUid: String,
        updatedData: UserDTO,
        allowRoleChange: Boolean = false,
        allowNameChange: Boolean = false // 👈 add a flag
    ): Boolean = transaction {
        val user = UserEntity.find { Users.firebaseUid eq firebaseUid }.firstOrNull()
            ?: return@transaction false.also {
                println("updateUser: User with UID $firebaseUid not found")
            }

        var changed = false

        // Only update name if explicitly allowed (e.g., from profile edit, not auth sync)
        if (allowNameChange) {
            updatedData.name?.let { name ->
                if (name != user.name) {
                    println("updateUser: Changing name from '${user.name}' to '$name'")
                    user.name = name
                    changed = true
                }
            }
        }

        updatedData.email?.let { email ->
            if (email != user.email) {
                println("updateUser: Changing email from '${user.email}' to '$email'")
                user.email = email
                changed = true
            }
        }

        updatedData.phone?.let { phone ->
            if (phone != user.phone) {
                println("updateUser: Changing phone from '${user.phone}' to '$phone'")
                user.phone = phone
                changed = true
            }
        }

        // role logic (leave as is) ...

        if (!changed) {
            println("updateUser: No changes detected for UID $firebaseUid, skipping update.")
        }

        changed
    }

    fun updateUsers(update: UpdateUserDTO): Boolean = transaction {
        val user = UserEntity.find { Users.firebaseUid eq update.firebaseUid }.firstOrNull()
            ?: return@transaction false.also {
                println("updateUser: User with UID ${update.firebaseUid} not found")
            }

        var changed = false

        update.name?.let { name ->
            if (name != user.name) {
                println("updateUser: Changing name from '${user.name}' to '$name'")
                user.name = name
                changed = true
            }
        }

        if (update.phone != user.phone) {
            if (update.phone == null) {
                println("updateUser: Deleting phone for UID ${update.firebaseUid}")
            } else {
                println("updateUser: Changing phone from '${user.phone}' to '${update.phone}'")
            }
            user.phone = update.phone
            changed = true
        }

        if (!changed) {
            println("updateUser: No changes detected for UID ${update.firebaseUid}")
        }

        changed
    }




    fun deleteUser(firebaseUid: String): Boolean = transaction {
        UserEntity.find { Users.firebaseUid eq firebaseUid }
            .firstOrNull()
            ?.apply { delete() } != null
    }



    fun createUser(dto: UserDTO): UserEntity = transaction {
        val newUser = UserEntity.new {
            this.firebaseUid = dto.firebaseUid
            this.email = dto.email!!
            this.name = dto.name
            this.phone = dto.phone
            this.role = dto.role ?: Role.USER
            this.createdAt = Instant.now()
        }

        val email = dto.email ?: throw IllegalArgumentException("Email is required")

        //Send welcome email
        EmailService.sendMail(
            to = email,
            subject = "Welcome to Catalyst!",
            body = """
            Hi ${dto.name ?: ""},
            
            Welcome to Catalyst! Your account has been successfully created.
            
            Thank you for joining us.
            
            — Catalyst Team
        """.trimIndent()
        )

        newUser
    }

    fun requireAdmin(user: UserEntity): Boolean {
        return if (user.role != Role.ADMIN) {
            println("❌ Unauthorized access by ${user.email}")
            false
        } else true
    }

    fun getUserWithEcoPoints(userId: Int): UserDTO? = transaction {
        (Users leftJoin UserEcoPoints)
            .slice(
                Users.id,
                Users.firebaseUid,
                Users.email,
                Users.name,
                Users.phone,
                Users.role,
                Users.createdAt,
                UserEcoPoints.points
            )
            .select { Users.id eq userId }
            .map { row ->
                UserDTO(
                    id = row[Users.id].value, // <-- unwrap EntityID<Int> to Int
                    firebaseUid = row[Users.firebaseUid],
                    email = row[Users.email],
                    name = row[Users.name],
                    phone = row[Users.phone],
                    roleRaw = row[Users.role].name, // since it's enum
                    createdAt = row[Users.createdAt].toString(),
                    ecoPoints = row[UserEcoPoints.points] ?: 0
                )
            }
            .singleOrNull()
    }

}