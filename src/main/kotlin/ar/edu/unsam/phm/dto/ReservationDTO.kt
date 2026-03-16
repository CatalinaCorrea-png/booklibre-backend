package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.domain.ReviewDTO
import java.time.LocalDate
import ar.edu.unsam.phm.domain.Reservation

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

data class ReservationProfileDTO(
    // Capaz se puede omitir el id de la reserva
    // Pero tambien puede estar bueno que te lleve al libro reservado
    val id: Int,
    val book: ProfileBookDTO,
    val state: String
)

fun Reservation.toReservationProfileDTO(): ReservationProfileDTO {
    val temporaryReserve: Reservation = Reservation()
    val isAvailable = !this.dateOverlaps(temporaryReserve)

    val reservationProfileDTO = ReservationProfileDTO(
        id = this.id,
        book = this.book.toProfileBookDTO(),
        state = if (isAvailable) State.AVAILABLE.value else State.BORROWED.value
    )
    return reservationProfileDTO
}