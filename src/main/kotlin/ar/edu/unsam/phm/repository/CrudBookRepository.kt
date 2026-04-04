package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface CrudBookRepository: CrudRepository<Book, Long> {
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

    //trae todos los libros que no tienen el borrado logico, es decir todos los libros que no fueron borrados
    //hay que usar este metodo sino va a traer libros que puede que hayan sido borrados ojooo
    fun findAllByDeletedAtIsNull(): List<Book>
}