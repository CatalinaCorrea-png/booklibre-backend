package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.User
import java.time.LocalDate

data class BookDTO(
    var id: String,
    var title: String,
    var bookType: String, // Para saber que clase instanciar (a chequear despues si usamos un jackson o algo de eso)
    var desc: String,
    var gender: String,
    var authorName: String,
    var authorAvatarUrl: String,
    var numPages: Int,
    val isbn: String,
    var language: String,
    var editorial: String,
    var publishDate: LocalDate,
    var condition: String,
    var owner: OwnerDTO,
    var imageSrc: String,
    var bookBibliokarmas: Long = 0,
    var rating: Double = 0.0,
    var lastTwoReviews: List<ReviewDTO> = emptyList(),
)

fun Book.toDTO(): BookDTO {
    val bookDTO = BookDTO(
        id = this.id!!,
        title = this.title,
        desc = this.desc,
        gender = this.gender.value,
        authorName = this.author.name,
        authorAvatarUrl = this.author.avatar,
        numPages = this.numPages,
        isbn = this.isbn,
        language = this.language.value,
        editorial = this.editorial,
        publishDate = this.publishDate,
        condition = this.condition.value,
        owner = this.owner,
        imageSrc = this.imageSrc,
        bookType = this.bookType,
        rating = this.ratingAvg,
        lastTwoReviews = this.lastTwoReviews,
    )
    return bookDTO
}

data class BookCreateDTO(
    val ownerId: String,
    val book: Book
)

fun BookCreateDTO.createFromDTO(owner: User): Book = this.book.apply { this.owner = owner.toOwnerDTO() }


fun Book.toBookCreateDTO() = BookCreateDTO(
    ownerId = this.owner.id,
    book = this
)