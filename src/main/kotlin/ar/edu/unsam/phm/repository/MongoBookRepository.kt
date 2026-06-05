package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface MongoBookRepository : MongoRepository<Book, String>, MongoBookRepositoryCustom {
    fun findByTitle(title: String): Optional<Book>
    fun findFirstByTitle(title: String): Optional<Book>
    fun findByIsbn(isbn: String): MutableList<Book>
    fun findByBookId(bookId: String): Optional<Book>
    fun findAllByBookIdIn(bookIds: List<String>): List<Book>
    fun findAllByOwnerId(ownerId: String): List<Book>

    // Libros que tienen al menos un click. Para sembrar el ZSET de ranking al arrancar.
    fun findByBookClicksGreaterThan(clicks: Int): List<Book>

}