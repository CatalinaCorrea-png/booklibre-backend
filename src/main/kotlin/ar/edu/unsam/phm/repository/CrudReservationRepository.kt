package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface CrudReservationRepository: CrudRepository<Reservation, Long> {

    fun findAllByBookOwnerId(userId: Long): Optional<List<Reservation>>

}