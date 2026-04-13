package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity
data class Reservation(
    @ManyToOne
    var user: User = User(),
    @ManyToOne
    var book: Book = Common(),
    //@OneToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var rate: Int = 0, // algo asi ponele
    var canRateReview: Boolean = true,
    var pickUpDate: LocalDate = LocalDate.now(),
    var dropOffDate: LocalDate = LocalDate.now(),
) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    val state: State get() = calculateState() // se recalcula cada vez que se accede, lo saco de el constructor

    fun reservationDays(): Int = ChronoUnit.DAYS.between(pickUpDate, dropOffDate).toInt() + 1

    // Se superponen si:
    // El inicio de A NO es después del fin de B
    // Y el inicio de B NO es después del fin de A
    fun dateOverlaps(reservation: Reservation): Boolean =
        !this.pickUpDate.isAfter(reservation.dropOffDate) && !reservation.pickUpDate.isAfter(this.dropOffDate)
    // Versión con .isBefore() (Si termina justo donde empieza otra, NO cuenta como traslape)

    fun isSoonToEnd() = this.dropOffDate.minusDays(2) == LocalDate.now()

    fun calculateState(): State {
        val today = LocalDate.now()
        return when {
            today.isAfter(dropOffDate) -> State.RETURNED
            today.isBefore(pickUpDate) -> State.RESERVED
            dropOffDate.minusDays(2) <= today -> State.SOON_TO_END
            else -> State.ACTIVE
        }
    }

    fun rateReview() {
        canRateReview = false
    }

    fun bookOwnerId(): Long = this.book.owner.id!!

    fun holderId(): Long = this.user.id!!

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

    override fun validate() {
        isPickUpBeforeDropOff() && isPickUpNotBeforeToday()
    }

    fun isActive(): Boolean = state == State.BORROWED || state == State.SOON_TO_END
}