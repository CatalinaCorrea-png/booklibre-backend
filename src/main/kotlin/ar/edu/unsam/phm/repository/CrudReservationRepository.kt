package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
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
}