package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
data class Review(
    @Column
    var reviewerName: String = "",
    @Column
    var rating: Int = 0,
    @Column
    var review: String = "",
    @Column
    var timestamp: LocalDate = LocalDate.now(),
    ) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun validate() {
        TODO("Not yet implemented")
    }

    fun notEmptyReview(): Boolean = this.rating > 0
}