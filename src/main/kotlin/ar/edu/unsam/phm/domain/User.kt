package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.UpdateUserProfileDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.RepositoryElement

class User(
    val name: String = "",
    val description: String = "",
    val email: String = "",
    val cel: String = "",
    val location: String = "",
    var userType: UserTypes = UserTypes.COMBINED,
    val timestamp: String = "",
    var bibliokarmas: Int = 0,
    var password: String = "",
//    var books: MutableList<Book> = mutableListOf<Book>(),
    var img: String = ""

): RepositoryElement {
    override var id = 0

    override fun meetsSearchCriteria(criteria: String) =
        matchesPartiallyWith(criteria, name) || matchesPartiallyWith(criteria, email)


    override fun meetsCreationCriteria() {
        if (!isNotEmpty(name)) throw NotFoundException("El usuario tiene que tener un nombre")
        if (!isNotEmpty(email)) throw NotFoundException("El usuario tiene que tener email")
    }

    companion object {
        fun fromDTO(userDTO: UpdateUserProfileDTO): User =
            User(
                name = userDTO.name,
                description = userDTO.description,
                email = userDTO.email,
                cel = userDTO.cel,
                location = userDTO.location,
                timestamp = userDTO.timestamp,
                bibliokarmas = userDTO.bibliokarmas,
                userType = UserTypes.fromValue(userDTO.userType),
            ).apply {
                this.id = userDTO.id
            }
    }
}