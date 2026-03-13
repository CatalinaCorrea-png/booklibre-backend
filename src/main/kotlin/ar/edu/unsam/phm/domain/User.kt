package ar.edu.unsam.phm.domain

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
//    var books: MutableList<Book> = mutableListOf<Book>(),
): RepositoryElement {
    override var id = 0

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}