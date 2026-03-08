package model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Reservation (
    var user: User,
    var pickUpDate: LocalDate,
    var dropOffDate: LocalDate
) {
    fun diasDeReserva(): Int = ChronoUnit.DAYS.between(pickUpDate, dropOffDate).toInt()
}