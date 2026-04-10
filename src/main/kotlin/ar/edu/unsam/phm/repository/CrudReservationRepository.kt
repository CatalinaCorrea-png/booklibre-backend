package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CrudReservationRepository: CrudRepository<Reservation, Long> {


    // Reservas donde el usuario es el LECTOR
    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.book b
        JOIN FETCH b.owner
        JOIN FETCH r.user u
        WHERE u.id = :userId
        AND b.deleted = false
        AND (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findByLectorIdFiltered(
        @Param("userId") userId: Long,
        @Param("search") search: String
    ): List<Reservation>

    // Reservas donde el usuario es el OWNER
    // Si vas a usar un campo en el WHERE, siempre asignale un alias en el JOIN
    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.book b
        JOIN FETCH b.owner o
        JOIN FETCH b.owner
        WHERE o.id = :userId
        AND b.deleted = false
        AND (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findByOwnerIdFiltered(
        @Param("userId") userId: Long,
        @Param("search") search: String
    ): List<Reservation>

//aca agrego query por que necesito filtrar que solo traiga los libros que no fueron eliminados
    @Query("""
    SELECT r
    FROM Reservation r
    JOIN r.book b
    WHERE b.owner.id = :userId
    AND b.deleted = false
""")
    fun findAllByBookOwnerId(userId: Long): List<Reservation>

    //para traer las reservas que tengan ese libro
    fun findByBookId(bookId: Long): List<Reservation>

    // lo hago asi, para no traer to.do a memoria para hacer un .size en el service y para delegar responsabilidades
    // JPQL retorna long por defecto supuestamente
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book.id = :bookId")
    fun countByBookId(@Param("bookId") bookId: Long): Int

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