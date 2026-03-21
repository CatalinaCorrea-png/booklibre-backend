package ar.edu.unsam.phm.dto
import ar.edu.unsam.phm.domain.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BookDTO(
    var id: Int,
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
    var reservationsIds: MutableList<Int>,
    var owner: UserDTO,
    var imageSrc: String,
    var bookBibliokarmas: Int = 0
    ) {

    fun fromDTO(): Book {
        return Common(
            title= this.title,
            desc= this.desc,
            gender= Gender.valueOf(this.gender),
            author= Author(this.authorName, this.authorAvatarUrl),
            numPages = this.numPages,
            isbn=  this.isbn,
            language = Language.valueOf(this.language),
            editorial= this.editorial,
            publishDate = this.publishDate,
            condition= BookCondition.valueOf(this.condition),
            reservationsIds = this.reservationsIds, // Las pido acá... ??
            owner= this.owner.fromDTO(),
            imageSrc = this.imageSrc,

        ).apply {
            id = this@BookDTO.id
        }
    }

}

fun Book.toDTO(): BookDTO{
    val bookDTO = BookDTO(
        id = this.id,
        title = this.title,
        desc = this.desc,
        gender = this.gender.value,
        authorName = this.author.name,
        authorAvatarUrl = this.author.avatar,
//        bookType = "COMUN",
        numPages = this.numPages,
        isbn=  this.isbn,
        language = this.language.value,
        editorial = this.editorial,
        publishDate = this.publishDate,
        condition = this.condition.value,
        reservationsIds = this.reservationsIds,
        owner = this.owner.toUserDTO(),
        imageSrc = this.imageSrc,
        bookType =  this.bookType,
    )
    return bookDTO
}


data class ProfileBookDTO(
    var id: Int,
    var title: String,
    var authorName: String,
    var gender: String,
    var timestamp: LocalDate,
    var imageSrc: String
)

fun Book.toProfileBookDTO(): ProfileBookDTO {
    val formatter = DateTimeFormatter.ofPattern("d MMM, yyyy")
    val profileBookDTO = ProfileBookDTO(
        id = this.id,
        title = this.title,
        authorName = this.author.name,
        gender = this.gender.value,
        timestamp = this.timestamp,
        imageSrc = this.imageSrc
    )
    return profileBookDTO
}

//hago un DTO aparte para la creacion, ya que en el create no necesito ID, ni reservations ID ni owner
data class BookCreateDTO(
    val title: String = "",
    val bookType: String = "",
    val desc: String = "",
    val gender: String = "",
    val book: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val numPages: Int = 0,
    val isbn: String = "",
    val language: String = "",
    val editorial: String = "",
    val publishDate: LocalDate? = null,
    val condition: String = "",
    val imageSrc: String = "",
    val ownerId: Int = 0
)

fun BookCreateDTO.createFromDTO(owner: User): Book {
    return Common(
        title = this.title,
        desc = this.desc,
        gender = Gender.fromValue(this.gender),
        author = Author(this.authorName, this.authorAvatarUrl),
        numPages = this.numPages,
        isbn = this.isbn,
        language = Language.fromValue(this.language),
        editorial = this.editorial,
        publishDate = this.publishDate ?: LocalDate.now(),
        condition = BookCondition.fromValue(this.condition),
        reservationsIds = mutableListOf(),
        owner = owner,
        imageSrc = this.imageSrc
    )
}


