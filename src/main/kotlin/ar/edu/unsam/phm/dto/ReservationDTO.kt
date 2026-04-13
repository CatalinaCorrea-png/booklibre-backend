package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.State
import java.time.LocalDate

data class ReservationDTO(
    val book: BookDTO,
    var id: Long,
    var user: UserDTO,
    var review: Int,
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
        book = this.book.toDTO(),
        id = this.id!!,
        user = this.user.toUserDTO(),
        review = this.rate,
        pickUpDate = this.pickUpDate,
        dropOffDate = this.dropOffDate,
        state = this.state,
        canRate = this.canRateReview, // cambiar a que se pueda calificar cuando sea null
        bibliokarmas = 0, // this.book.calculateBibliokarmas(days, user.bibliokarmas, ), //todo: arreglar esto...
        // se pisa en el service con el valor correcto
        loanedBy = this.book.owner.name,
        loanedTo = this.user.name,
    )
}