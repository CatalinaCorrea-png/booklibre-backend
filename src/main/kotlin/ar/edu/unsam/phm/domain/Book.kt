package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.repository.RepositoryElement
import java.time.LocalDate

abstract class Book (
    var title: String = "",
    var desc: String = "",
    var gender: Gender = Gender.DRAMA,
    var author: Author = Author("", ""),
    var numPages: Int = 0,
    var isbn: String = "978-3-16-148410-0",
    var language: Language = Language.SPANISH,
    var editorial: String = "",
    var publishDate: LocalDate = LocalDate.now(),
    var condition: BookCondition = BookCondition.EXCELLENT,
    var reservationsIds: MutableList<Int> = mutableListOf(),
    var owner: User = User(),
    var imageSrc: String = "",
    var timestamp: LocalDate = LocalDate.now()

): RepositoryElement {
    override var id = 0

    fun addReservation(reservationId: Int) {
        reservationsIds.add(reservationId)
    }

    // Template Method Primitiva
    fun calculateBibliokarmas(reservation: Reservation) : Int = 5 * reservation.reservationDays() + typeBibliokarmas(reservation)

    // different for every type of book
    abstract fun typeBibliokarmas(reservation: Reservation) : Int

    override fun meetsSearchCriteria(criteria: String) : Boolean {
        TODO()
    }

    override fun meetsCreationCriteria() {
        TODO()
    }
}