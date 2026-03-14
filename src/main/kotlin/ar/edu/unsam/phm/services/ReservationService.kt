package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import org.springframework.stereotype.Service

@Service
class ReservationService(
    val reservationRepository: ReservationRepository,
    val bookRepository: BookRepository
){
    fun createReservation(reservation: Reservation) {
        if (!canReserve(reservation)) throw BusinessException("Reserva no disponible en esa fecha")
        reservationRepository.create(reservation)
    }

    // esto tiene que estar negado asi devuelve true si no hay solapamiento
    fun canReserve(reservation: Reservation) : Boolean = reservationRepository.repositoryObjects().any { !it.dateOverlaps(reservation) }

    fun getAvailableReservations(reservation: Reservation) : MutableList<Reservation> {
        return reservationRepository.repositoryObjects().filter { it.dateOverlaps(reservation) }.toMutableList()
    }

    fun getReservesByUserId(userId: Int, search: String): List<Reservation> {
        return reservationRepository.findByLectorId(userId).filter { res ->
            res.book.title.contains(search, ignoreCase = true) ||
                    res.book.author.name.contains(search, ignoreCase = true)
        }
    }

    fun getLoansMadeByUserId(userId: Int, search: String): List<Reservation> {
        return reservationRepository.findByOwnerId(userId).filter { res ->
            res.book.title.contains(search, ignoreCase = true) ||
                    res.book.author.name.contains(search, ignoreCase = true)
        }
    }

    fun rateLoan(reservationId: Int, puntuacion: Int, comentario: String) {
        val reservation = reservationRepository.getObject(reservationId)

        // le pongo la review desde aca, no se si esta bien
        reservation.review.apply {
            this.rating = puntuacion
            this.comment = comentario
        }

        //! acordate de actualizarlo bobo
        reservationRepository.update(reservation)
    }
}