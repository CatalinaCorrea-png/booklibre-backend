package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.OwnerDTO
import ar.edu.unsam.phm.dto.ReservationDatesDTO
import ar.edu.unsam.phm.dto.ReviewDTO
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.RepositoryElement
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

//@Entity
@Document(collection = "books")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "bookType"
)
@JsonSubTypes(
    Type(value = Common::class, name = "COMUN"),
    Type(value = WithADedication::class, name = "CON DEDICATORIA"),
    Type(value = Collectable::class, name = "COLECCIONABLE"),
)
abstract class Book(
    var title: String = "",
    var desc: String = "",
    var gender: Gender = Gender.DRAMA,
    var author: Author = Author("",""), // Se mapea solo
    var numPages: Int = 0,
    var isbn: String = "978-3-16-148410-0",
    var language: Language = Language.SPANISH,
    var editorial: String = "",
    var publishDate: LocalDate = LocalDate.now(),
    var condition: BookCondition = BookCondition.EXCELLENT,
    var owner: OwnerDTO = OwnerDTO(),
    var imageSrc: String = "",
    // Fecha de ALTA del libro en el sistema (cuándo se publicó en la plataforma),
    // distinta de publishDate (fecha de edición/publicación real del libro).
    // La usan Home (caché), perfil y el ordenamiento de "Mis libros".
    var createdAt: LocalDate = LocalDate.now(),
    // Timestamp con hora del alta, EXCLUSIVO del feed de actividad reciente. Necesita
    // hora (no solo fecha) para mostrar "recién" y ordenar altas del mismo día por el
    // momento real. Es additivo: no afecta a createdAt ni a las otras páginas.
    // NULLABLE a propósito: un libro viejo que no tiene el campo queda en null (no en
    // now()), así no aparece como "recién" en el feed; los null ordenan al final.
    var registeredAt: LocalDateTime? = null,
    var bookType: String,
    var deleted: Boolean = false,
    var ratingAvg: Double = 0.0,
    // @field:JsonProperty fuerza a Jackson a serializar/deserializar este campo private
    // (su getter no es un bean getter), necesario para que reservationCount sobreviva
    // el round-trip por Redis y WithADedication calcule bien sus bibliokarmas.
    @field:JsonProperty("reservationCount")
    private var reservationCount: Long = 0,
    var reservations: MutableList<ReservationDatesDTO> = mutableListOf(),
    var lastTwoReviews: MutableList<ReviewDTO> = mutableListOf(),
    var bookClicks: Int = 0

    ) : RepositoryElement {


    @Id
    override var id: String? = null
    var bookId: String = UUID.randomUUID().toString()  // FK lógica

    fun logicDelete() {
        deleted = true
    }

    // Template Method Primitiva
    fun calculateBibliokarmas(reservationDays: Int, userBibliokarmas: Long): Long =
        5 * reservationDays + typeBibliokarmas(userBibliokarmas)

    // different for every type of book
    abstract fun typeBibliokarmas(userBibliokarmas: Long): Long

//    fun addReview(review: Review) {
//        if (review.rating !in 1..5) throw ConflictException("Ingrese una calificaión entre 1 y 5")
//        reviews.add(review)
//        updateRating()
//    }
//
    fun updateRating(newRating: Double) {
        this.ratingAvg = newRating
    }

    fun reservationCount(newCount: Long) {
        this.reservationCount = newCount
    }

    fun addReservation(reservationDatesDTO: ReservationDatesDTO) {
        reservations.add(reservationDatesDTO)
    }

    fun ownerIsReader(): Boolean = owner.userType == UserTypes.READER

    override fun validate() {
        if (!isNotEmpty(title)) throw ConflictException("El libro tiene que tener titulo")
        if (title.length > 50) throw ConflictException("El titulo no debe superar los 50 caracteres")
        if (!isNotEmpty(desc)) throw ConflictException("El libro tiene que tener descripcion")
        if (desc.length > 1000) throw ConflictException("La descripcion no debe superar los 1000 caracteres")
        if (!isNotEmpty(author.toString())) throw ConflictException("El libro tiene que tener autor")
        if (numPages <= 0) throw ConflictException("El libro tiene que tener cantidad de paginas")
        if (numPages > 2000) throw ConflictException("El libro no puede superar las 2000 paginas")
        if (!isNotEmpty(isbn)) throw ConflictException("El libro tiene que tener ISBN")
        if (!isNotEmpty(editorial)) throw ConflictException("El libro tiene que tener editorial")
        if (editorial.length > 50) throw ConflictException("La editorial no debe superar los 50 caracteres")
        if (!isNotEmpty(imageSrc)) throw ConflictException("El libro tiene que tener imagen de referencia")
        if (imageSrc.length >= 255) throw ConflictException("La imagen del libro tiene demasiados caracteres. Max. 255")
        if (!isNotEmpty(author.name)) throw ConflictException("El libro tiene que tener un autor")
        if ((author.name).length > 50) throw ConflictException("El nombre del autor no debe superar los 50 caracteres")
        if (ownerIsReader()) throw ConflictException("Si sos lector no podes crear un libro. Cambia tu rol.")
    }

    override fun meetsSearchCriteria(criteria: String): Boolean =
        criteria.isBlank() ||
                this.title.contains(criteria.trim(), ignoreCase = true)
//              || this.author.name.contains(criteria.trim(), ignoreCase = true)

    fun reservationCount(): Long = this.reservationCount

    fun numPagesLong() : Long = this.numPages.toLong()

}