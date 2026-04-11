package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.dto.ProfileBookDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ar.edu.unsam.phm.domain.Gender
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.util.Optional

interface CrudBookRepository: CrudRepository<Book, Long> {
    fun findByIsbn(isbn: String): Optional<Book>

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

    @Query("""
        SELECT new ar.edu.unsam.phm.dto.ProfileBookDTO(
            b.id,
            b.title,
            b.author.name,
            b.gender,
            b.timestamp,
            b.imageSrc,
            CASE
                WHEN r IS NOT NULL THEN 'PRESTADO'
                ELSE 'DISPONIBLE'
            END        
            )
        FROM Book b
        LEFT JOIN Reservation r 
        ON r.book = b
        AND r.pickUpDate <= CURRENT_DATE
        AND r.dropOffDate >= CURRENT_DATE
        WHERE b.owner.id = :userId
        AND b.deleted = false
            AND (
                :filterBy = 'ALL'
                OR (:filterBy = 'AVAILABLE' AND r IS NULL)
                OR (:filterBy = 'BORROWED' AND r IS NOT NULL)
            )

    """)
    fun getAllUserBooks(userId: Long?, pageable: Pageable, filterBy: String): Page<ProfileBookDTO>

    //trae todos los libros que no tienen el borrado logico, es decir todos los libros que no fueron borrados
    //hay que usar este metodo sino va a traer libros que puede que hayan sido borrados ojooo
    fun findAllByDeletedIsFalse(): List<Book>
}