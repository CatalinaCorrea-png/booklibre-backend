package ar.edu.unsam.phm

import ar.edu.unsam.phm.repository.CrudReservationRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BookReservationInitializer(val repoReservations: CrudReservationRepository) {
    @Transactional
    fun initBookReservationsIds() {
        repoReservations.findAll().forEach { reservation ->
            reservation.book.addReservation(reservation.id!!)
        }
    }
}