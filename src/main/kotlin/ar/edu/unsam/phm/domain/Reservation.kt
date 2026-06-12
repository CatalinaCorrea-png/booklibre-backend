package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.RepositoryElement
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
data class Reservation(
    @ManyToOne(fetch = FetchType.LAZY)
    var user: User = User(),
//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "book_id", nullable = false)
    var bookId: String = "",
    @Transient
    var book: Book? = null, // NO se persiste, se carga desde Mongo en el service
    var pickUpDate: LocalDate = LocalDate.now(),
    var dropOffDate: LocalDate = LocalDate.now(),
    var bookTitle: String = "",
    var bookAuthorName: String = "",
    var bookImageSrc: String = "",
    var ownerName: String = "",
    var ownerId: String = "",
    var bookDeleted: Boolean = false,
    var bibliokarmas: Long = 0,
    // Fecha en que se confirmó la reserva (distinta de pickUpDate, que es cuándo se retira
    // el libro). Es la "fecha de la reserva" que usa el feed de actividad reciente.
    var createdAt: LocalDateTime = LocalDateTime.now(),
) : RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: String? = null

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
        return if (this.book?.ownerIsReader()!!) {
            throw BusinessException("Este libro ya no está disponible.")
        } else true
    }

    override fun meetsSearchCriteria(criteria: String): Boolean {
        TODO("Not yet implemented")
    }

    private fun userIsNotOwner(): Boolean {
        return if (this.book?.owner?.id == this.user.id) {
            throw BusinessException("No podes reservar un libro si sos el dueño.")
        } else true
    }

    override fun validate() {
        isPickUpBeforeDropOff() && isPickUpNotBeforeToday() && ownerIsReader() && userIsPublisher() && userIsNotOwner()
    }

}