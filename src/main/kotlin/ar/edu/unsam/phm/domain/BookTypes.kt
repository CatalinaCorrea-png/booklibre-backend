package ar.edu.unsam.phm.domain

import java.time.LocalDate

class Common(
    title: String = "",
    desc: String = "",
    gender: Gender = Gender.DRAMA,
    author: Author = Author("", ""),
    numPages: Int = 0,
    isbn: String = "978-3-16-148410-0",
    language: Language = Language.SPANISH,
    editorial: String = "",
    publishDate: LocalDate = LocalDate.now(),
    condition: BookCondition = BookCondition.EXCELLENT,
    reservationsIds: MutableList<Int> = mutableListOf(),
    owner: User = User(),
    imageSrc: String = ""
)
    : Book(title, desc, gender, author, numPages, isbn, language, editorial, publishDate, condition, reservationsIds, owner) {
    override fun typeBibliokarmas(reservation: Reservation) : Int = if (reservation.user.bibliokarmas < 1000) this.numPages * 5 else this.numPages * 2
}

class WithADedication(
    title: String = "",
    desc: String = "",
    gender: Gender = Gender.DRAMA,
    author: Author = Author("", ""),
    numPages: Int = 0,
    isbn: String = "978-3-16-148410-0",
    language: Language = Language.SPANISH,
    editorial: String = "",
    publishDate: LocalDate = LocalDate.now(),
    condition: BookCondition = BookCondition.EXCELLENT,
    reservationsIds: MutableList<Int> = mutableListOf(),
    owner: User = User(),
    imageSrc: String = ""
)
    : Book(title, desc, gender, author, numPages, isbn, language, editorial, publishDate, condition, reservationsIds, owner) {
    override fun typeBibliokarmas(reservation: Reservation): Int = 200 * 10 * this.reservationsIds.size
}

class Collectable(
    title: String = "",
    desc: String = "",
    gender: Gender = Gender.DRAMA,
    author: Author = Author("", ""),
    numPages: Int = 0,
    isbn: String = "978-3-16-148410-0",
    language: Language = Language.SPANISH,
    editorial: String = "",
    publishDate: LocalDate = LocalDate.now(),
    condition: BookCondition = BookCondition.EXCELLENT,
    reservationsIds: MutableList<Int> = mutableListOf(),
    owner: User = User(),
    imageSrc: String = ""
)
    : Book(title, desc, gender, author, numPages, isbn, language, editorial, publishDate, condition, reservationsIds, owner) {
    override fun typeBibliokarmas(reservation: Reservation): Int = reservation.user.bibliokarmas / 5 + this.numPages
}