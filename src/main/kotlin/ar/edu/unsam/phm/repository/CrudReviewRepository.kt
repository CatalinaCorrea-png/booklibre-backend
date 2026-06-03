package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Review
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.query.Param

interface ReservationRatingProjection {
    val reservationId: String
    val rating: Int
}

@Repository
interface CrudReviewRepository : CrudRepository<Review, String> {

    fun findByReservationId(reservationId: String): Review?

    fun findAllByReservationIdIn(reservationIds: Collection<String>): List<Review>

    @Query("SELECT r.reservation.id AS reservationId, r.rating AS rating FROM Review r WHERE r.reservation.id IN :ids")
    fun findRatingsByReservationIdIn(ids: List<String>): List<ReservationRatingProjection>

//    @Query("SELECT r FROM Review r WHERE r.bookId = :bookId")
//    fun findAllByBookId(@Param("bookId") bookId: String, pageable: Pageable): Page<Review>

    fun findReviewsByBookId(bookId: String, pageable: Pageable): Page<Review>

    fun findAllByBookId(bookId: String): List<Review>
}