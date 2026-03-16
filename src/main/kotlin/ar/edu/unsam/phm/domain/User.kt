package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.RepositoryElement

enum class UserType(val value: String){
    PUBLISHER("Publicador"),
    READER("Lector"),
    COMBINED("Lector / Publicador");

    companion object {
        fun fromValue(value: String): UserType {
            return entries.find { it.value == value }
                ?: throw ConflictException("UserType desconocido: $value")
        }
    }
}

class User(
    val name: String = "",
    val description: String = "",
    val email: String = "",
    val cel: String = "",
    val location: String = "",
    var userType: UserType = UserType.COMBINED,
    val timestamp: String = "",
    var bibliokarmas: Int = 0,
    var img: String = ""

): RepositoryElement {
    override var id = 0

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}