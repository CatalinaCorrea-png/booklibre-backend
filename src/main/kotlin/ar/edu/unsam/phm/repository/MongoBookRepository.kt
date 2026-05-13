package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface CustomBookRepository {
    fun findByCriteria(criteria: Criteria, pageable: Pageable): Page<Book>

}

class MongoBookRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : CustomBookRepository {
    override fun findByCriteria(criteria: Criteria, pageable: Pageable): Page<Book> {
        val query = Query(criteria).with(pageable)
        val books = mongoTemplate.find(query, Book::class.java)
        val total = mongoTemplate.count(Query(criteria), Book::class.java)
        return PageImpl(books, pageable, total)
    }
}

interface MongoBookRepository : MongoRepository<Book, String>, CustomBookRepository {
    fun findByTitle(title: String): Optional<Book>
    fun findByIsbn(isbn: String): MutableList<Book>
    fun findAllByBookIdIn(bookIds: List<String>): List<Book>

}