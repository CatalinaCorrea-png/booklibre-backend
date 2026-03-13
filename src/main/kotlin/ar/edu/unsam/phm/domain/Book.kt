package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.repository.RepositoryElement
import java.time.LocalDate

enum class Gender(val value: String) {
    DRAMA("DRAMA"),
    SCIENCE_FICTION("SCIENCIA FICCION"),
    ROMANCE("ROMANCE"),
    SELF_HELP("AUTO AYUDA"),
    DESIGN("DISEÑO"),
    CLASSIC_LITERATURE("LITERATURA_CLASICA")
}

enum class Language(val value: String) {
    SPANISH("ESPAÑOL"),
    ENGLISH("INGLES"),
    FRENCH("FRANCES"),
    PORTUGUESE("PORTUGUES")
}

enum class BookCondition(val value: String) {
    EXCELLENT("EXCELENTE"),
    VERY_GOOD("MUY BUENO"),
    GOOD("BUENO"),
    BAD("MALO"),
    REGULAR("REGULAR")
}

abstract class Book (
    var title: String = "",
    var desc: String = "",
    var gender: Gender = Gender.DRAMA,
    var author: Author = Author("", ""),
    var numPages: Int = 0,
    val ISBN: String = "978-3-16-148410-0",
    var language: Language = Language.SPANISH,
    var editorial: String = "",
    var publishDate: LocalDate = LocalDate.now(),
    var condition: BookCondition = BookCondition.EXCELLENT,
    var reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
    var owner: User = User()
    ): RepositoryElement {
    override var id = 0

    // Template Method Primitiva
    fun calculateBibliokarmas(reservation: Reservation) : Int = 5 * reservation.reservationDays() + typeBibliokarmas(reservation)

    // different for every type of book
    abstract fun typeBibliokarmas(reservation: Reservation) : Int

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO()
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO()
    }

    fun toDTO() = BookDTO(title = "")
}

class Common : Book() {
    override fun typeBibliokarmas(reservation: Reservation) : Int = if (reservation.user.bibliokarmas < 1000) this.numPages * 5 else this.numPages * 2
}

class WithADedication : Book() {
    override fun typeBibliokarmas(reservation: Reservation): Int = 200 * 10 * this.reservations.size
}

class Collectable : Book() {
    override fun typeBibliokarmas(reservation: Reservation): Int = reservation.user.bibliokarmas / 5 + this.numPages
}