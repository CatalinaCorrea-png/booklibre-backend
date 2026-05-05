package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Review
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.query.Param

interface ReservationRatingProjection {
    val reservationId: Long
    val rating: Int
}

@Repository
interface CrudReviewRepository : CrudRepository<Review, Long> {

    fun findByReservationId(reservationId: Long): Review?

    fun findAllByReservationIdIn(reservationIds: Collection<Long>): List<Review>

    @Query("SELECT r.reservation.id AS reservationId, r.rating AS rating FROM Review r WHERE r.reservation.id IN :ids")
    fun findRatingsByReservationIdIn(ids: List<Long>): List<ReservationRatingProjection>

    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId")
    fun findAllByBookId(@Param("bookId") bookId: Long, pageable: Pageable): Page<Review>
}