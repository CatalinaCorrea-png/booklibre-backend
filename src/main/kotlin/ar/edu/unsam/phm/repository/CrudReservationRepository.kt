package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface CrudReservationRepository: CrudRepository<Reservation, Long> {
//aca agrego query por que necesito filtrar que solo traiga los libros que no fueron eliminados
    @Query("""
    SELECT r
    FROM Reservation r
    JOIN r.book b
    WHERE b.owner.id = :userId
    AND b.deletedAt IS NULL
""")
    fun findAllByBookOwnerId(userId: Long): List<Reservation>

    //para traer las reservas que tengan ese libro
    fun findByBookId(bookId: Long): List<Reservation>

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