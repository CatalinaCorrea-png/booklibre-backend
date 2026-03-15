package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.repository.RepositoryElement
import java.time.LocalDate

enum class Gender(val value: String) {
    DRAMA("Drama"),
    SCIENCE_FICTION("Ciencia Ficcion"),
    ROMANCE("Romance"),
    SELF_HELP("Auto Ayuda"),
    DESIGN("Diseño"),
    CLASSIC_LITERATURE("Literatura Clasica")
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
    var reservationsIds: MutableList<Int> = mutableListOf(),
    var owner: User = User(),
    var imageSrc: String = ""
    ): RepositoryElement {
    override var id = 0

    fun addReservation(reservationId: Int) {
        reservationsIds.add(reservationId)
    }

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
}

class Common(
    title: String = "",
    desc: String = "",
    gender: Gender = Gender.DRAMA,
    author: Author = Author("", ""),
    numPages: Int = 0,
    ISBN: String = "978-3-16-148410-0",
    language: Language = Language.SPANISH,
    editorial: String = "",
    publishDate: LocalDate = LocalDate.now(),
    condition: BookCondition = BookCondition.EXCELLENT,
    reservationsIds: MutableList<Int> = mutableListOf(),
    owner: User = User(),
    imageSrc: String = ""
)
    : Book(title, desc, gender, author, numPages, ISBN, language, editorial, publishDate, condition, reservationsIds, owner) {
    override fun typeBibliokarmas(reservation: Reservation) : Int = if (reservation.user.bibliokarmas < 1000) this.numPages * 5 else this.numPages * 2
}

class WithADedication(
    title: String = "",
    desc: String = "",
    gender: Gender = Gender.DRAMA,
    author: Author = Author("", ""),
    numPages: Int = 0,
    ISBN: String = "978-3-16-148410-0",
    language: Language = Language.SPANISH,
    editorial: String = "",
    publishDate: LocalDate = LocalDate.now(),
    condition: BookCondition = BookCondition.EXCELLENT,
    reservationsIds: MutableList<Int> = mutableListOf(),
    owner: User = User(),
    imageSrc: String = ""
)
    : Book(title, desc, gender, author, numPages, ISBN, language, editorial, publishDate, condition, reservationsIds, owner) {
    override fun typeBibliokarmas(reservation: Reservation): Int = 200 * 10 * this.reservationsIds.size
}

class Collectable(
    title: String = "",
    desc: String = "",
    gender: Gender = Gender.DRAMA,
    author: Author = Author("", ""),
    numPages: Int = 0,
    ISBN: String = "978-3-16-148410-0",
    language: Language = Language.SPANISH,
    editorial: String = "",
    publishDate: LocalDate = LocalDate.now(),
    condition: BookCondition = BookCondition.EXCELLENT,
    reservationsIds: MutableList<Int> = mutableListOf(),
    owner: User = User(),
    imageSrc: String = ""
)
    : Book(title, desc, gender, author, numPages, ISBN, language, editorial, publishDate, condition, reservationsIds, owner) {
    override fun typeBibliokarmas(reservation: Reservation): Int = reservation.user.bibliokarmas / 5 + this.numPages
}