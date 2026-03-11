package model

import errors.BusinessException

class User(
    val name: String = "",
    val description: String = "",
    val email: String = "",
    val cel: String = "",
    val location: String = "",
    var userType: UserType = Combined(),
    val timestamp: String = "",
    var bibliokarmas: Int = 0,
    val ownBooks: MutableList<Book> = mutableListOf(),
    val readBooks: Int = 0

): RepositoryElement {
    override var id = 0

    fun reserveBook(book: Book, reservation: Reservation) {
        book.reserve(reservation)
        this.bibliokarmas += book.calculateBibliokarmas(reservation)
    }

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}