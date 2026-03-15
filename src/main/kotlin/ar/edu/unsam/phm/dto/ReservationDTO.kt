package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.domain.ReviewDTO
import java.time.LocalDate

data class ReservationDTO (
    val book: BookDTO,
    var id: Int,
    var user: UserDTO,
    var review: ReviewDTO,
    var pickUpDate: LocalDate,
    var dropOffDate: LocalDate,
    var state: String
) {

    fun fromDTO(): Reservation {
        return Reservation(
            user= this.user.fromDTO(),
            book= this.book.fromDTO(),
            review= this.review.fromDTO(),
            pickUpDate= this.pickUpDate,
            dropOffDate= this.dropOffDate,
            state= State.valueOf(this.state),
        ).apply {
            id = this@ReservationDTO.id
        }
    }

}

fun Reservation.toDTO() : ReservationDTO {
    return ReservationDTO(
        book = this.book.toDTO(),
        id = this.id,
        user = this.user.toUserDTO(),
        review =  this.review.toDTO(),
        pickUpDate = this.pickUpDate,
        dropOffDate = this.dropOffDate,
        state = this.state.value
    )
}