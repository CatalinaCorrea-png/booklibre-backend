package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface CrudReservationRepository: CrudRepository<Reservation, Long> {

    fun findAllByBookOwnerId(userId: Long): Optional<List<Reservation>>

    fun findByBookId(bookId: Long): List<Reservation>

    @Query("""
        SELECT r.review.rating
        FROM Reservation r
        WHERE r.book.id = :bookId
    """)
    fun findRatingsByBookId(bookId: Long): List<Int>
    @Query("""
    SELECT COUNT(r) > 0
    FROM Reservation r
    WHERE r.book.id = :bookId
    AND r.pickUpDate < :dropOffDate
    AND r.dropOffDate > :pickUpDate
    """)

    fun hasOverlappingReservation(
        @Param("bookId") bookId: Long,
        @Param("pickUpDate") pickUpDate: LocalDate,
        @Param("dropOffDate") dropOffDate: LocalDate
    ): Boolean

    fun findAllByBookId(bookId: Long): List<Reservation>

    @Query("""
    SELECT r.review
    FROM Reservation r
    WHERE r.book.id = :bookId
    """)
    fun findAllReviewsByBookId(@Param("bookId") bookId: Long): List<Review>
}