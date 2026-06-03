package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes

data class UserDTO(
    val id: String,
    val name: String,
    val description: String,
    val email: String,
    val cel: String,
    val location: String,
    val timestamp: String,
    val bibliokarmas: Long,
    val userType: String,
    val img: String
) {
    fun fromDTO(): User {
        return User(
            name = this.name,
            description = this.description,
            email = this.email,
            cel = this.cel,
            location = this.location,
            userType = UserTypes.fromValue(this.userType),
            timestamp = this.timestamp,
            bibliokarmas = this.bibliokarmas,
            img = this.img
        )
    }
}

data class UpdateUserProfileDTO(
    val id: String,
    val name: String,
    val description: String,
    val img: String,
    val email: String,
    val cel: String,
    val location: String,
    val timestamp: String,
    val bibliokarmas: Long,
    val userType: String
)

fun User.toUserDTO(): UserDTO {
    return UserDTO(
        id = this.id!!,
        name = this.name,
        description = this.description,
        email = this.email,
        cel = this.cel,
        location = this.location,
        timestamp = this.timestamp,
        bibliokarmas = this.bibliokarmas,
        userType = this.userType.value,
        img = this.img
    )
}

data class UpdateProfileResponse(
    val user: UserDTO,
    val accessToken: String
)

data class OwnerDTO(
    val id: String = "",
    val name: String = "",
    val bibliokarmas: Long = 0,
    val userType: UserTypes = UserTypes.READER,
    val img: String = ""
)
fun User.toOwnerDTO() : OwnerDTO {
    return OwnerDTO(
        id = this.id!!,
        name = this.name,
        bibliokarmas = this.bibliokarmas,
        userType = this.userType,
        img = this.img
    )
}