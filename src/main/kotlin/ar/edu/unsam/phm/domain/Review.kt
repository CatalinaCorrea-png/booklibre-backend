package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.repository.RepositoryElement
import java.time.LocalDate

// Mandar id del usuario por params para el nombre
data class Review(
    var reviewerName: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var timestamp: LocalDate = LocalDate.now(),

): RepositoryElement {
    override var id: Int = 0

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria() {
        TODO("Not yet implemented")
    }
}

data class ReviewDTO(
    var id: Int,
    var reviewerName: String,
    var rating: Int,
    var comment: String,
    var timestamp: String
){}

fun Review.toDTO(): ReviewDTO {
    return ReviewDTO(
        id = this.id,
        reviewerName = this.reviewerName,
        rating = this.rating,
        comment = this.comment,
        timestamp = this.timestamp.toString()
    )
}
