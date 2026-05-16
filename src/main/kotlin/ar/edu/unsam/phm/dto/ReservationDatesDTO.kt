package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Reservation
import java.time.LocalDate

data class ReservationDatesDTO (
    val resevationId: String,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate,
)

fun Reservation.toReservationDate(): ReservationDatesDTO = ReservationDatesDTO(
    resevationId = this.id!!,
    pickUpDate = this.pickUpDate,
    dropOffDate = this.dropOffDate
)