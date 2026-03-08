package model

import errors.BusinessException

class User(
    val name: String = "",
    val description: String = "",
    val email: String = "",
    val cel: String = "",
    val location: String = "",
    var userType: UserType,
    val timestamp: String = "",
    var bibliokarmas: Int = 0,
    val ownBooks: MutableList<Book> = mutableListOf(),
    val readBooks: Int = 0

): RepositoryElement {
    override var id = 0

    fun reserveBook(book: Book, reservation: Reservation) {
        if(!book.canReserve(reservation)) throw BusinessException("Reserva no disponible en esas fechas")
        book.addReservation(reservation)
        this.bibliokarmas += book.calculateBibliokarmas(reservation)
    }

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}