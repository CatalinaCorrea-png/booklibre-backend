//package ar.edu.unsam.phm.repository
//
//import ar.edu.unsam.phm.domain.Book
//import ar.edu.unsam.phm.dto.ProfileBookDTO
//import org.springframework.data.domain.Page
//import org.springframework.data.domain.Pageable
//import ar.edu.unsam.phm.domain.Gender
//import org.springframework.data.jpa.domain.Specification
//import org.springframework.data.jpa.repository.EntityGraph
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor
//import org.springframework.data.jpa.repository.Query
//import org.springframework.data.repository.CrudRepository
//import java.util.*
//
//interface CrudBookRepository : CrudRepository<Book, String>, JpaSpecificationExecutor<Book> {
//    fun findByIsbn(isbn: String): Optional<Book>
//
//    @EntityGraph(attributePaths = ["owner", "author"])
//    override fun findAll(spec: Specification<Book>, pageable: Pageable): Page<Book>
//
//    @EntityGraph(attributePaths = ["owner", "author"])
//    override fun findById(id: String): Optional<Book?>
//
//    @Query(
//        """
//        SELECT new ar.edu.unsam.phm.dto.ProfileBookDTO(
//            b.id,
//            b.title,
//            b.author.name,
//            b.gender,
//            b.timestamp,
//            b.imageSrc,
//            CASE
//                WHEN r IS NOT NULL THEN 'PRESTADO'
//                ELSE 'DISPONIBLE'
//            END
//            )
//        FROM Book b
//        LEFT JOIN Reservation r
//        ON r.book = b
//        AND r.pickUpDate <= CURRENT_DATE
//        AND r.dropOffDate >= CURRENT_DATE
//        WHERE b.owner.id = :userId
//        AND b.deleted = false
//            AND (
//                :filterBy = 'ALL'
//                OR (:filterBy = 'AVAILABLE' AND r IS NULL)
//                OR (:filterBy = 'BORROWED' AND r IS NOT NULL)
//            )
//    """
//    )
//    fun getAllUserBooks(userId: Long?, pageable: Pageable, filterBy: String): Page<ProfileBookDTO>
//
//    //trae todos los libros que no tienen el borrado logico, es decir todos los libros que no fueron borrados
//    //hay que usar este metodo sino va a traer libros que puede que hayan sido borrados ojooo
//    fun findAllByDeletedIsFalse(): List<Book>
//}