package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Reservation
import java.time.LocalDate

data class CreateReservationDTO(
    val bookId: Int,
    val userId: Int,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate
){}
