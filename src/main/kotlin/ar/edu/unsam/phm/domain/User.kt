package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.RepositoryElement

enum class UserType(val value: String){
    PUBLISHER("Publisher"),
    READER("Reader"),
    COMBINED("Combined")
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
    var password: String = "",
//    var books: MutableList<Book> = mutableListOf<Book>(),
): RepositoryElement {
    override var id = 0

    override fun meetsSearchCriteria(criteria: String) =
        matchesPartiallyWith(criteria, name) || matchesPartiallyWith(criteria, email)


    override fun meetsCreationCriteria() {
        if (!isNotEmpty(name)) throw NotFoundException("El usuario tiene que tener un nombre")
        if (!isNotEmpty(email)) throw NotFoundException("El usuario tiene que tener email")
    }
}