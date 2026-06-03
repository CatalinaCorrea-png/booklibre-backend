package ar.edu.unsam.phm.dto

import java.time.LocalDate

data class CreateReservationDTO(
    val bookId: String,
    val sessionId: String,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate
) {}
