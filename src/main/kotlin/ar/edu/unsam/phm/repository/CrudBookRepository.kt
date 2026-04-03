package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Gender
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
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

    @Query("""
        SELECT b
        FROM Book b
        WHERE (:userId <> b.owner.id)
        AND (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:ownersName IS NULL OR LOWER(b.owner.name) LIKE LOWER(CONCAT('%', :ownersName, '%')))
        AND (:isbn IS NULL OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :isbn, '%')))
        AND (:pagesRangeMin IS NULL OR b.numPages >= :pagesRangeMin)
        AND (:pagesRangeMax IS NULL OR b.numPages <= :pagesRangeMax)
        AND (:#{#genders.isEmpty()} = true OR b.gender IN :genders)
        AND NOT EXISTS (
            SELECT r
            FROM Reservation r
            WHERE r.book = b
            AND r.pickUpDate <= CAST(:dropOffDate AS date)
            AND r.dropOffDate >= CAST(:pickUpDate AS date)
        )        
    """)
    fun findAllByCriteria(
        userId: Long?,
        title: String?,
        genders: List<Gender>,
        pagesRangeMin: Int?,
        pagesRangeMax: Int?,
        pickUpDate: LocalDate,
        dropOffDate: LocalDate,
        isbn: String?,
        ownersName: String?,
        pageable: Pageable
    ): Page<Book>
}