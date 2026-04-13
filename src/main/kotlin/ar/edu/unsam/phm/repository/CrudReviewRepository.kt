package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Review
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CrudReviewRepository : CrudRepository<Review, Long> {

    fun findByReservationId(reservationId: Long): Review?

    fun findAllByReservationIdIn(reservationIds: Collection<Long>): List<Review>

}