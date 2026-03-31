//package ar.edu.unsam.phm.repository
//
//import ar.edu.unsam.phm.domain.Book
//import org.springframework.data.repository.CrudRepository
//import java.util.Optional
//
//interface CrudBookRepository: CrudRepository<Book, Int> {
//    fun findByIsbn(isbn: String): Optional<Book>
//}