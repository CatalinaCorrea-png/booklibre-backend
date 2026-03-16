package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.repository.RepositoryElement
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class State(val value: String){
    AVAILABLE("ACTIVO"),
    BORROWED("PRESTADO"),
    SOON_TO_END("PROXIMO A VENCER"),
    RETURNED("DEVUELTO")
}

data class Reservation (
    var user: User,
    var book: Book,
    var review: Review,
    var pickUpDate: LocalDate = LocalDate.now(),
    var dropOffDate: LocalDate = LocalDate.now(),
    var state: State = State.AVAILABLE
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

    override fun meetsCreationCriteria() {
        TODO("Not yet implemented")
    }
}