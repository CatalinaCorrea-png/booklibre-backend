package dto

import model.UserType
import model.User

data class UserDTO(
    val id: Int,
    val name: String,
    val description: String,
    val email: String,
    val cel: String,
    val location: String,
    val timestamp: String,
    val bibliokarmas: Int,
    val ownBooks: List<BookDTO>,
    val readBooks: Int,
    val userType: UserType
)

fun User.toUserDTO(): UserDTO {
    return UserDTO(
        id = this.id,
        name = this.name,
        description = this.description,
        email = this.email,
        cel = this.cel,
        location = this.location,
        timestamp = this.timestamp,
        bibliokarmas = this.bibliokarmas,
        ownBooks = this.ownBooks.map { it.toDTO() },
        readBooks = this.readBooks,
        userType = this.userType
    )
}