package ar.edu.unsam.phm.dto
import ar.edu.unsam.phm.domain.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BookDTO(
    var id: Long,
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
    var owner: UserDTO,
    var imageSrc: String,
    var bookBibliokarmas: Int = 0,
    var rating: Double = 0.0
)

fun Book.toDTO(): BookDTO{
    val bookDTO = BookDTO(
        id = this.id!!,
        title = this.title,
        desc = this.desc,
        gender = this.gender.value,
        authorName = this.author.name,
        authorAvatarUrl = this.author.avatar,
        numPages = this.numPages,
        isbn=  this.isbn,
        language = this.language.value,
        editorial = this.editorial,
        publishDate = this.publishDate,
        condition = this.condition.value,
        owner = this.owner.toUserDTO(),
        imageSrc = this.imageSrc,
        bookType =  this.bookType
    )
    return bookDTO
}

//hago un DTO aparte para la creacion, ya que en el create no necesito ID, ni reservations ID ni owner
data class BookCreateDTO(
    val title: String = "",
    val desc: String = "",
    val gender: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val numPages: Int = 0,
    val isbn: String = "",
    val language: String = "",
    val editorial: String = "",
    val publishDate: LocalDate? = null,
    val condition: String = "",
    val imageSrc: String = "",
    val ownerId: Long? = null,
    val book: Book
)

fun BookCreateDTO.createFromDTO(owner:User): Book = this.book.apply { this.owner = owner }


fun Book.toBookCreateDTO() = BookCreateDTO(
    title        = this.title,
    desc         = this.desc,
    gender       = this.gender.value,
    authorName   = this.author.name,
    authorAvatarUrl = this.author.avatar,
    numPages     = this.numPages,
    isbn         = this.isbn,
    language     = this.language.value,
    editorial    = this.editorial,
    publishDate  = this.publishDate,
    condition    = this.condition.value,
    imageSrc     = this.imageSrc,
    ownerId      = this.owner.id,
    book         = this                  // toma el bookType que Jackson serializa
)