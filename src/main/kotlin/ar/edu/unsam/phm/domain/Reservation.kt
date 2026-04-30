package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.*
import org.hibernate.usertype.UserType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity
data class Reservation(
    @ManyToOne(fetch = FetchType.LAZY)
    var user: User = User(),
    @ManyToOne(fetch = FetchType.LAZY)
    var book: Book = Common(),
    var pickUpDate: LocalDate = LocalDate.now(),
    var dropOffDate: LocalDate = LocalDate.now(),
) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    val state: State
        get() = State.get(
            LocalDate.now(),
            pickUpDate,
            dropOffDate
        )// se recalcula cada vez que se accede, lo saco de el constructor

    fun reservationDays(): Int = ChronoUnit.DAYS.between(pickUpDate, dropOffDate).toInt() + 1

    // Se superponen si:
    // El inicio de A NO es después del fin de B
    // Y el inicio de B NO es después del fin de A
    fun dateOverlaps(reservation: Reservation): Boolean =
        !this.pickUpDate.isAfter(reservation.dropOffDate) && !reservation.pickUpDate.isAfter(this.dropOffDate)
    // Versión con .isBefore() (Si termina justo donde empieza otra, NO cuenta como traslape)

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

    private fun userIsPublisher(): Boolean {
        return if (this.user.userType == UserTypes.PUBLISHER) {
            throw BusinessException("Un publicador no puede reservar libros. Cambia tu rol a lector o combinado.")
        } else true
    }

    private fun ownerIsReader(): Boolean {
        return if (this.book.ownerIsReader()) {
            throw BusinessException("Este libro ya no está disponible.")
        } else true
    }

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun validate() {
        isPickUpBeforeDropOff() && isPickUpNotBeforeToday() && ownerIsReader() && userIsPublisher()
    }

}