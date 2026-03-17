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
    ) {

    fun fromDTO(): Book {
        if (this.bookType == "COMUN") {
            // return Common()
        }
        if (this.bookType == "CON DEDICATORIA") {
            // return WithADedication()
        }
        if (this.bookType == "COLECCIONABLE") {
            // return Collectable()
        }
        // Por ahora...
        return Common(
            title= this.title,
            desc= this.desc,
            gender= Gender.DRAMA,
            author= Author(this.authorName, this.authorAvatarUrl),
            numPages = this.numPages,
            isbn=  this.isbn,
            language = Language.valueOf(this.language),
            editorial= this.editorial,
            publishDate = this.publishDate,
            condition= BookCondition.valueOf(this.condition),
            reservationsIds = this.reservationsIds, // Las pido acá... ??
            owner= this.owner.fromDTO(),
            imageSrc = this.imageSrc
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
        bookType = "COMUN",
        numPages = this.numPages,
        isbn=  this.isbn,
        language = this.language.value,
        editorial = this.editorial,
        publishDate = this.publishDate,
        condition = this.condition.value,
        reservationsIds = this.reservationsIds,
        owner = this.owner.toUserDTO(),
        imageSrc = this.imageSrc
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


