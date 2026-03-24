package ar.edu.unsam.phm.dto

import java.time.LocalDate

data class ReservedPeriodDTO(
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate
)