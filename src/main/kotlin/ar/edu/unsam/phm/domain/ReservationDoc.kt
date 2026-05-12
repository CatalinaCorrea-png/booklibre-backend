package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "reservations")
data class ReservationDoc(
    @Id var id: String,
    val userId: String,
    val bookId: String,
    val ownerId: String,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate
)

fun Reservation.toDoc(ownerId: String): ReservationDoc =
    ReservationDoc(
        id = id ?: throw BusinessException("Se tiene que persistir la reserva en Postgre antes de generarla en Mongo. - Reservation.toDoc() exception"),
        userId = user.id!!,
        bookId = bookId,
        ownerId = ownerId,
        pickUpDate = pickUpDate,
        dropOffDate = dropOffDate
    )
