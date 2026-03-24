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
    imageSrc: String = "",
    timestamp: LocalDate = LocalDate.now(),
    bookType: String = "COMUN"
)
    : Book(title, desc, gender, author, numPages, isbn, language, editorial, publishDate, condition, reservationsIds, owner, imageSrc, timestamp, bookType) {
    override fun typeBibliokarmas(userBibliokarmas: Int) : Int = if (userBibliokarmas < 1000) this.numPages * 5 else this.numPages * 2
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
    imageSrc: String = "",
    timestamp: LocalDate = LocalDate.now(),
    bookType: String = "CON DEDICATORIA"
)
    : Book(title, desc, gender, author, numPages, isbn, language, editorial, publishDate, condition, reservationsIds, owner,  imageSrc, timestamp, bookType) {
    override fun typeBibliokarmas(userBibliokarmas: Int): Int = 200 + 10 * this.reservationsIds.size
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
    imageSrc: String = "",
    timestamp: LocalDate = LocalDate.now(),
    bookType: String = "COLECCIONABLE"
)
    : Book(title, desc, gender, author, numPages, isbn, language, editorial, publishDate, condition, reservationsIds, owner,  imageSrc, timestamp, bookType) {
    override fun typeBibliokarmas(userBibliokarmas: Int): Int {
        // redondeo hacia arriba
        val fifthPart = (userBibliokarmas + 4) / 5
        return fifthPart + this.numPages
    }
}