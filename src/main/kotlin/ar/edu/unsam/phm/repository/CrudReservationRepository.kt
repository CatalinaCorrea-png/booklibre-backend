package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface CrudReservationRepository: CrudRepository<Reservation, Long> {

    fun findAllByBookOwnerId(userId: Long): List<Reservation>

    @Query("""
         SELECT count(r)
         FROM Reservation r
         WHERE r.book.owner.id = :userId
         AND r.pickUpDate <= CURRENT_DATE
         AND r.dropOffDate >= CURRENT_DATE 
    """)
    fun countUserReservedBooks(userId: Long): Long

    @Query("""
        SELECT count(r)
        FROM Reservation r
        WHERE r.user.id = :userId
        AND r.dropOffDate < CURRENT_DATE
    """)
    fun countUserReadBooksNumber(userId: Long): Long
}