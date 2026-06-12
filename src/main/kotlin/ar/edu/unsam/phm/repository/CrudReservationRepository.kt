package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.UserTypes
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface CrudReservationRepository : CrudRepository<Reservation, String> {
    // Reservas donde el usuario es el LECTOR
    // esta es la solucion de dodino para el problema de N + 1 Querys
    // EntityGraph solo sobre "user" porque "book" es @Transient — los datos del libro vienen de Mongo en el service
    @EntityGraph(attributePaths = ["user"])
    @Query(
        """
    SELECT r FROM Reservation r
    WHERE r.user.id = :userId
    AND r.user.userType <> :userType
    AND (
        :search = ''
        OR LOWER(r.bookTitle) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(r.bookAuthorName) LIKE LOWER(CONCAT('%', :search, '%'))
    )"""
    )
    fun findByLectorIdFiltered(
        @Param("userId") userId: String,
        @Param("search") search: String,
        @Param("userType") userType: UserTypes,
        pageable: Pageable
    ): Page<Reservation>

    // Reservas donde el usuario es el OWNER
    // Si vas a usar un campo en el WHERE, siempre asignale un alias en el JOIN
    @EntityGraph(attributePaths = ["user"])
    @Query(
        """
    SELECT r FROM Reservation r
    WHERE r.ownerId = :userId
    AND r.user.userType <> :userType
    AND (
        :search = ''
        OR LOWER(r.bookTitle) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(r.bookAuthorName) LIKE LOWER(CONCAT('%', :search, '%'))
    )"""
    )
    fun findByOwnerIdFiltered(
        @Param("userId") userId: String,
        @Param("search") search: String,
        @Param("userType") userType: UserTypes,
        pageable: Pageable
    ): Page<Reservation>

    //para traer las reservas que tengan ese libro
    fun findByBookId(bookId: String): List<Reservation>

    @Query(
        """
    SELECT COUNT(r) > 0
    FROM Reservation r
    WHERE r.bookId = :bookId
    AND r.pickUpDate < :dropOffDate
    AND r.dropOffDate > :pickUpDate
    """
    )
    fun hasOverlappingReservation(
        @Param("bookId") bookId: String,
        @Param("pickUpDate") pickUpDate: LocalDate,
        @Param("dropOffDate") dropOffDate: LocalDate
    ): Boolean

    fun findAllByBookId(bookId: String): List<Reservation>

    // lo hago asi, para no traer to.do a memoria para hacer un .size en el service y para delegar responsabilidades
    // JPQL retorna long por defecto supuestamente
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.bookId = :bookId")
    fun countByBookId(@Param("bookId") bookId: String): Long

    @Query("SELECT r.bookId, COUNT(r) FROM Reservation r WHERE r.bookId IN :bookIds GROUP BY r.bookId")
    fun countByBookIds(@Param("bookIds") bookIds: List<String>): List<Array<Any>>

    @Query(
        """
            SELECT count(r)
            FROM Reservation r
            WHERE r.ownerId = :userId
            AND r.pickUpDate <= CURRENT_DATE 
            AND r.dropOffDate >= CURRENT_DATE
        """
    )
    fun countUserReservedBooks(userId: String): Long

    @Query(
        """
        SELECT count(r)
        FROM Reservation r
        WHERE r.user.id = :userId
        AND r.dropOffDate < CURRENT_DATE
    """
    )
    fun countUserReadBooksNumber(userId: String): Long
    fun findAllByUser_Id(userId: String): MutableList<Reservation>

    @Modifying
    @Query("UPDATE Reservation r SET r.bookDeleted = true WHERE r.bookId = :bookId")
    fun markBookAsDeletedInReservations(@Param("bookId") bookId: String)
    fun findByOwnerId(ownerId: String): MutableList<Reservation>

    // Top 5 reservas confirmadas más recientes. @EntityGraph trae al user en la misma
    // query: es LAZY y el resolver lee user.name (sin esto: LazyInitializationException).
    @EntityGraph(attributePaths = ["user"])
    fun findTop5ByOrderByCreatedAtDesc(): List<Reservation>
}