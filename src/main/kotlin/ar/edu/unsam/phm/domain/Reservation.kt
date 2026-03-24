package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.RepositoryElement
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Reservation (
    var user: User = User(),
    var book: Book = Common(),
    var review: Review = Review(),
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

    fun bookOwnerId(): Int = this.book.owner.id

    fun holderId(): Int = this.user.id

    private fun isPickUpNotBeforeToday(): Boolean {
        return if (this.pickUpDate.isBefore(LocalDate.now()))
            throw BusinessException("La fecha de recogida no puede ser anterior a hoy")
        else true
    }

    private fun isPickUpBeforeDropOff(): Boolean {
        return if (this.dropOffDate.isBefore(this.pickUpDate))
            throw BusinessException("No se puede reservar un libro si su fecha de devolucion es antes que su recogida")
        else true
    }
    
    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun meetsCreationCriteria() {
        TODO("Not yet implemented")
    }

    fun validate(){
        isPickUpBeforeDropOff() && isPickUpNotBeforeToday()
    }
}