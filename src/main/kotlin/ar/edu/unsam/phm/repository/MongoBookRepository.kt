package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BookTesting
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MongoBookRepository : MongoRepository<BookTesting, String> {
    fun findByTitulo(titulo: String): BookTesting?
}