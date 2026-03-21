package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.RepositoryElement
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

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
abstract class Book (
    var title: String = "",
    var desc: String = "",
    var gender: Gender = Gender.DRAMA,
    var author: Author = Author("", ""),
    var numPages: Int = 0,
    var isbn: String = "978-3-16-148410-0",
    var language: Language = Language.SPANISH,
    var editorial: String = "",
    var publishDate: LocalDate = LocalDate.now(),
    var condition: BookCondition = BookCondition.EXCELLENT,
    var reservationsIds: MutableList<Int> = mutableListOf(),
    var owner: User = User(),
    var imageSrc: String = "",
    var timestamp: LocalDate = LocalDate.now(),
    val bookType: String
): RepositoryElement {
    override var id = 0

    fun addReservation(reservationId: Int) {
        reservationsIds.add(reservationId)
    }

    // Template Method Primitiva
    fun calculateBibliokarmas(reservation: Reservation) : Int = 5 * reservation.reservationDays() + typeBibliokarmas(reservation)

    // different for every type of book
    abstract fun typeBibliokarmas(reservation: Reservation) : Int

    override fun meetsSearchCriteria(criteria: String) : Boolean {
        TODO()
    }

    override fun meetsCreationCriteria() {
        if (!isNotEmpty(title)) throw ConflictException("El libro tiene que tener titulo")
        if (!isNotEmpty(desc)) throw ConflictException("El libro tiene que tener descripcion")
        if (!isNotEmpty(author.toString())) throw ConflictException("El libro tiene que tener autor")
        if (numPages <= 0) throw ConflictException("El libro tiene que tener cantidad de paginas")
        if (!isNotEmpty(isbn)) throw ConflictException("El libro tiene que tener ISBN")
        if (!isNotEmpty(editorial)) throw ConflictException("El libro tiene que tener editorial")
        if (!isNotEmpty(imageSrc)) throw ConflictException("El libro tiene que tener imagen de referencia")
    }


}