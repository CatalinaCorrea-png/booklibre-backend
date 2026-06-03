package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.dto.toUserDTO
import ar.edu.unsam.phm.errors.BusinessException
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Document(collection = "reservations")
data class ReservationDoc(
    @Id var id: String,
    val userId: String,
    val bookId: String,
    val ownerId: String,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate,
    // snapshot del libro
    val bookTitle: String = "",
    val bookAuthorName: String = "",
    val bookImageSrc: String = "",
    val bookDeleted: Boolean = false,
    // snapshot del owner (loanedBy)
    val ownerName: String = "",
    // snapshot del user (loanedTo)
    val userName: String = "",
)

fun Reservation.toDoc(ownerId: String): ReservationDoc {
    val book = book ?: throw BusinessException("La reserva necesita un book seteado antes de llamar toDoc()")
    return ReservationDoc(
        id = id ?: throw BusinessException("Se tiene que persistir la reserva en Postgre antes de generarla en Mongo."),
        userId = user.id!!,
        bookId = bookId,
        ownerId = ownerId,
        pickUpDate = pickUpDate,
        dropOffDate = dropOffDate,
        bookTitle = book.title,
        bookAuthorName = book.author?.name ?: "",
        bookImageSrc = book.imageSrc ?: "",
        bookDeleted = book.deleted,
        ownerName = book.owner?.name ?: "",
        userName = user.name,
    )
}

fun ReservationDoc.reservationDays(): Int =
    ChronoUnit.DAYS.between(pickUpDate, dropOffDate).toInt() + 1

val ReservationDoc.state: State
    get() = State.get(LocalDate.now(), pickUpDate, dropOffDate)

fun ReservationDoc.toDTO(
    hasReview: Boolean,
    book: Book,
    user: User,
): ReservationDTO {
    return ReservationDTO(
        book = book.toDTO(),
        id = this.id,
        user = user.toUserDTO(),
        review = 0,
        pickUpDate = this.pickUpDate,
        dropOffDate = this.dropOffDate,
        state = this.state,
        canRate = this.state == State.RETURNED && !hasReview,
        bibliokarmas = 0, // se pisa en el service
        loanedBy = book.owner.name,
        loanedTo = user.name,
    )
}