package model

import dto.BookDTO
import java.time.LocalDate

enum class Gender {
    DRAMA,
    CIENCIA_FICCION,
    ROMANCE,
    AUTOAYUDA,
    DISEÑO,
    LITERATURA_CLASICA
}

enum class Language {
    ESPAÑOL,
    INGLES,
    FRANCES,
    PORTUGUES
}

enum class BookCondition {
    EXCELENTE,
    MUY_BUENO,
    BUENO,
    MALO,
    REGULAR
}

abstract class Book (
    var title: String = "",
    var desc: String = "",
    var gender: Gender = Gender.DRAMA,
    var author: String = "",
    var numPages: Int = 0,
    val ISBN: String = "978-3-16-148410-0",
    var language: Language = Language.ESPAÑOL,
    var editorial: String = "",
    var publishDate: LocalDate = LocalDate.now(),
    var condition: BookCondition = BookCondition.EXCELENTE,
    var owner: User = User(userType = Reader),
    var reservations: MutableList<Reservation> = mutableListOf()
): RepositoryElement {
    override var id = 0

    // Template Method Primitiva
    fun calculateBibliokarmas(reservation: Reservation) : Int = 5 * reservation.reservationDays() + typeBibliokarmas()

    // different for every type of book
    abstract fun typeBibliokarmas() : Int

    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
    }

    fun canReserve(reservation: Reservation): Boolean =
        !reservations.any { it.dateOverlaps(reservation) }


    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO()
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO()
    }

    fun toDTO() = BookDTO(title= "")
}

class Common : Book() {
    override fun typeBibliokarmas() : Int = if (this.owner.bibliokarmas < 1000) this.numPages * 5 else this.numPages * 2
}

class WithDedication : Book() {
    override fun typeBibliokarmas(): Int = 200 + 10 * (this.reservations.size)
}

class Collectable : Book() {
    override fun typeBibliokarmas(): Int = this.owner.bibliokarmas / 5 + this.numPages
}