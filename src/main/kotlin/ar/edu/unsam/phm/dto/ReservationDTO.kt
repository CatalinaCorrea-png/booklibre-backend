package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.*
import java.time.LocalDate
import ar.edu.unsam.phm.domain.Reservation

data class ReservationDTO(
    val book: BookDTO,
    var id: Long,
    var user: UserDTO,
    var review: ReviewDTO,
    var pickUpDate: LocalDate,
    var dropOffDate: LocalDate,
    var state: State,
    var canRate: Boolean,
    var bibliokarmas: Int = 0,
    var loanedBy: String,
    var loanedTo: String,
)

fun Reservation.toDTO(): ReservationDTO {
    val days = Reservation(pickUpDate = pickUpDate, dropOffDate = dropOffDate).reservationDays()

    return ReservationDTO(
        book        = this.book.toDTO(),
        id          = this.id!!,
        user        = this.user.toUserDTO(),
        review      = this.review!!.toDTO(),
        pickUpDate  = this.pickUpDate,
        dropOffDate = this.dropOffDate,
        state       = this.state,
        canRate = this.state == State.RETURNED && this.review!!.rating == 0,
//        bibliokarmas = this.book.calculateBibliokarmas(days, ),
        loanedBy    = this.book.owner.name,
        loanedTo    = this.user.name,
    )
}

fun LocalDate.isBetween(start: LocalDate, end: LocalDate): Boolean =
    this.isAfter(start) && this.isBefore(end)

data class ReservationProfileDTO(
    val id: Long?, //lo hago nullable para que no revienten las reservas ficticias
    val book: ProfileBookDTO,
    var state: State
)

fun Reservation.toReservationProfileDTO(): ReservationProfileDTO {

    val reservationProfileDTO = ReservationProfileDTO(
        id = this.id,
        book = this.book.toProfileBookDTO(),
        state = this.state
    )
    return reservationProfileDTO
}