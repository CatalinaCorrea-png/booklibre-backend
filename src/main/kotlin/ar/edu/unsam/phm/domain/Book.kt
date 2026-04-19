package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.RepositoryElement
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import org.hibernate.annotations.Formula
import java.time.LocalDate

@Entity
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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Book(
    @Column(nullable = false)
    var title: String = "",

    @Column(name = "description", length = 1000, nullable = false)
    var desc: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender = Gender.DRAMA,

    // le decís a JPA: "no cargues esta relación hasta que alguien la pida explícitamente"
    @ManyToOne(fetch = FetchType.LAZY)
    var author: Author = Author("", ""),

    @Column(nullable = false)
    var numPages: Int = 0,

    @Column(length = 17, nullable = false)
    var isbn: String = "978-3-16-148410-0",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var language: Language = Language.SPANISH,

    @Column(nullable = false)
    var editorial: String = "",

    @Column(nullable = false)
    var publishDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var condition: BookCondition = BookCondition.EXCELLENT,

    @ManyToOne(fetch = FetchType.LAZY)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    var owner: User = User(),

    @Column(nullable = false)
    var imageSrc: String = "",

    @Column(nullable = false)
    var timestamp: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val bookType: String,

    //agrego esta columna para el delete logico
    @Column(name = "deleted")
    var deleted: Boolean = false,

    @OneToMany(
        mappedBy = "book",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
    ) // Lo cascadeo porque en esta implementación funciona asi...
    val reviews: MutableList<Review> = mutableListOf(),

    @Column
    var ratingAvg: Double = 0.0,

//    @ElementCollection(fetch = FetchType.LAZY)
//    private val _reservationsIds: MutableList<Long> = mutableListOf(),

//    @Formula("(SELECT COUNT(*) FROM reservation r WHERE r.book_id = {alias}.id)") // {alias} lo hace más compatible con otros motores
    @Formula("(SELECT COUNT(*) FROM reservation r WHERE r.book_id = id)")
    private var reservationCount: Long = 0,

    ) : RepositoryElement {

    @Id
    @GeneratedValue
    override var id: Long? = null

    fun logicDelete() {
        deleted = true
    }

    // Template Method Primitiva
    fun calculateBibliokarmas(reservationDays: Int, userBibliokarmas: Int): Long =
        5 * reservationDays + typeBibliokarmas(userBibliokarmas)

    // different for every type of book
    abstract fun typeBibliokarmas(userBibliokarmas: Int): Long

    fun addReview(review: Review) {
        if (review.rating !in 1..5) throw ConflictException("Ingrese una calificaión entre 1 y 5")
        reviews.add(review)
        updateRating()
    }

    private fun updateRating() {
        this.ratingAvg = reviews.map { it.rating }.average()
    }

    override fun validate() {
        if (!isNotEmpty(title)) throw ConflictException("El libro tiene que tener titulo")
        if (!isNotEmpty(desc)) throw ConflictException("El libro tiene que tener descripcion")
        if (desc.length > 500) throw ConflictException("La descripcion no debe superar los 500 caracteres")
        if (!isNotEmpty(author.toString())) throw ConflictException("El libro tiene que tener autor")
        if (numPages <= 0) throw ConflictException("El libro tiene que tener cantidad de paginas")
        if (!isNotEmpty(isbn)) throw ConflictException("El libro tiene que tener ISBN")
        if (!isNotEmpty(editorial)) throw ConflictException("El libro tiene que tener editorial")
        if (!isNotEmpty(imageSrc)) throw ConflictException("El libro tiene que tener imagen de referencia")
        if (imageSrc.length >= 255) throw ConflictException("La imagen del libro tiene demasiados caracteres. Max. 255")
    }

    override fun meetsSearchCriteria(criteria: String): Boolean =
        criteria.isBlank() ||
                this.title.contains(criteria.trim(), ignoreCase = true)
//              || this.author.name.contains(criteria.trim(), ignoreCase = true)

    fun reservationCount(): Long = this.reservationCount

    fun numPagesLong() : Long = this.numPages.toLong()

}