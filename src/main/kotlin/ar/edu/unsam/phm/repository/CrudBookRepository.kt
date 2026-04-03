package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface CrudBookRepository: CrudRepository<Book, Int> {
    fun findByIsbn(isbn: String): Optional<Book>

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.owner.id = :userId
        AND NOT EXISTS (
            SELECT r
            FROM Reservation r
            WHERE r.book = b
        )
    """)
    fun findBooksWithoutReservations(userId: Long): Optional<List<Book>>
}