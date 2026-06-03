package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Review

data class ReviewDTO(
    var id: String = "",
    var reviewerName: String = "",
    var rating: Int = 0,
    var review: String = "",
    var timestamp: String = "",
    var reservation: ReservationDTO? = null,
) {

//    fun fromDTO(): Review {
//        return Review(
//            reviewerName = this.reviewerName,
//            rating = this.rating,
//            review = this.review,
//            timestamp = LocalDate.parse(this.timestamp),
//            reservation = Reservation(),
//            book = Common()
//        ).apply {
//            id = this@ReviewDTO.id
//        }
//    }
}

fun Review.toDTO(): ReviewDTO {
    return ReviewDTO(
        id = this.id!!,
        reviewerName = this.reviewerName,
        rating = this.rating,
        review = this.review,
        timestamp = this.timestamp.toString()
    )
}