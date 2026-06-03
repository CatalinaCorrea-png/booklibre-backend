package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.*
import java.time.LocalDate

@Entity
data class Review(
    @Column
    var reviewerName: String = "",
    @Column
    var rating: Int = 0,
    @Column(length = 250)
    var review: String = "",
    @Column
    var timestamp: LocalDate = LocalDate.now(),

    @OneToOne // Esta es la MEJOR solucion de las que pense que tiene solucion a la mierda que hicimos
    @JoinColumn(name = "reservation_id")
    val reservation: Reservation,

    @Column(name = "book_id")
    val bookId: String = "",

    ) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: String? = null

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun validate() {
        if (this.rating !in 1..5) { throw BusinessException("La calificación debe estar entre 1 y 5.") }
        if (this.review.length > 250) { throw BusinessException("La reseña no debe superar los 250 caracteres.") }
    }

    fun notEmptyReview(): Boolean = this.rating > 1
}