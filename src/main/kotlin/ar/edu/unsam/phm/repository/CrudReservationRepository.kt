package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import ar.edu.unsam.phm.domain.Review
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface CrudReservationRepository : CrudRepository<Reservation, Long> {
    // Reservas donde el usuario es el LECTOR
    // esta es la solucion de dodino para el problema de N + 1 Querys
    @EntityGraph(
        attributePaths = [
            "book",
            "book.owner", // esto por que el dto necesita el nombre
            "book.author", // esto por el nombre de el autor para el filtro
            "review", // la review para el can rate
            "user"]
    )
    @Query(
        """
    SELECT r FROM Reservation r
    WHERE r.user.id = :userId
    AND r.book.deleted = false
    AND (
        LOWER(r.book.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(r.book.author.name) LIKE LOWER(CONCAT('%', :search, '%'))
    )"""
    )
    fun findByLectorIdFiltered(
        @Param("userId") userId: Long,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<Reservation> // todo: !important la tercera query es por esto
    // estos son de spring Data, lo tengo que usar si o si por que page me devuleve la cantidad de elementos y la cantidad de paginas segun el tamaño de la pagina

    // Reservas donde el usuario es el OWNER
    // Si vas a usar un campo en el WHERE, siempre asignale un alias en el JOIN
    @EntityGraph(
        attributePaths = [
            "book",
            "book.owner",
            "book.author",
            "review",
            "user"]
    )
    @Query(
        """
        SELECT r FROM Reservation r
        WHERE r.book.owner.id = :userId
        AND r.book.deleted = false
        AND (
            LOWER(r.book.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.book.author.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )"""
    )
    fun findByOwnerIdFiltered(
        @Param("userId") userId: Long,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<Reservation>

    //para traer las reservas que tengan ese libro
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
    // lo hago asi, para no traer to.do a memoria para hacer un .size en el service y para delegar responsabilidades
    // JPQL retorna long por defecto supuestamente
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book.id = :bookId")
    fun countByBookId(@Param("bookId") bookId: Long): Int

    @Query("SELECT r.book.id, COUNT(r) FROM Reservation r WHERE r.book.id IN :bookIds GROUP BY r.book.id")
    fun countByBookIds(@Param("bookIds") bookIds: List<Long>): List<Array<Any>>

    @Query(
        """
         SELECT count(r)
         FROM Reservation r
         WHERE r.book.owner.id = :userId
         AND r.pickUpDate <= CURRENT_DATE
         AND r.dropOffDate >= CURRENT_DATE 
    """
    )
    fun countUserReservedBooks(userId: Long): Long

    @Query(
        """
        SELECT count(r)
        FROM Reservation r
        WHERE r.user.id = :userId
        AND r.dropOffDate < CURRENT_DATE
    """
    )
    fun countUserReadBooksNumber(userId: Long): Long
}