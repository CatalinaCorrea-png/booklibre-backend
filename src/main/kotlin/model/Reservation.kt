package model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Reservation (
    var user: User,
    var pickUpDate: LocalDate = LocalDate.now(),
    var dropOffDate: LocalDate = LocalDate.now(),
): RepositoryElement {
    override var id = 0

    fun reservationDays(): Int = ChronoUnit.DAYS.between(pickUpDate, dropOffDate).toInt()

    // Se superponen si:
    // El inicio de A NO es después del fin de B
    // Y el inicio de B NO es después del fin de A
    fun dateOverlaps(reservation: Reservation): Boolean = !this.pickUpDate.isAfter(reservation.dropOffDate) && !reservation.pickUpDate.isAfter(this.dropOffDate)
    // Versión con .isBefore() (Si termina justo donde empieza otra, NO cuenta como traslape)

    fun isSoonToEnd() = this.dropOffDate.minusDays(2) == LocalDate.now()


    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria(): Boolean {
        TODO("Not yet implemented")
    }
}