package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation

@org.springframework.stereotype.Repository
class ReservationRepository: Repository<Reservation>() {

    fun findByLectorId(userId: Int): List<Reservation> =
        repositoryObjects().filter { it.user.id == userId }

    fun findByOwnerId(userId: Int): List<Reservation> =
        repositoryObjects().filter { it.book.owner.id == userId }
}