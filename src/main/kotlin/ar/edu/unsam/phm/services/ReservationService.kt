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

    fun canReserve(reservation: Reservation) : Boolean = reservationRepository.repositoryObjects().any { it.dateOverlaps(reservation) }

    fun getAvailableReservations(reservation: Reservation) : MutableList<Reservation> {
        return reservationRepository.repositoryObjects().filter { it.dateOverlaps(reservation) }.toMutableList()
    }
}