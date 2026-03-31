package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Reservation
import java.time.LocalDate

data class CreateReservationDTO(
    val bookId: Long,
    val sessionId: Long,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate
){}
