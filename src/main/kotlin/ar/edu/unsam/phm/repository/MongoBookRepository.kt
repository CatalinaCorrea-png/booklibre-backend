package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MongoBookRepository : MongoRepository<Book, String> {
    fun findByTitle(title: String): Optional<Book>
    fun findByIsbn(isbn: String): MutableList<Book>
}