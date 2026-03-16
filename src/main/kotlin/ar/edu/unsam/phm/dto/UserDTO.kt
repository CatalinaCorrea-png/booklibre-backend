package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserType

data class UserDTO(
    val id: Int,
    val name: String,
    val description: String,
    val email: String,
    val cel: String,
    val location: String,
    val timestamp: String,
    val bibliokarmas: Int,
    val userType: String,
    val img: String
) {
    fun fromDTO(): User {
        return User(
            name= this.name,
            description= this.description,
            email= this.email,
            cel= this.cel,
            location= this.location,
            userType= UserType.fromValue(this.userType),
            timestamp= this.timestamp,
            bibliokarmas= this.bibliokarmas,
            img = this.img
        )
    }
}

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
        userType = this.userType.value,
        img = this.img
    )
}