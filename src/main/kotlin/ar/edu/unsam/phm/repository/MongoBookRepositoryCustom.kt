package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria

interface MongoBookRepositoryCustom {
    fun findUserBooks(
        ownerId: String,
        filterCriteria: Criteria,
        pageable: Pageable
    ): Page<Book>

    fun findByCriteria(
        criteria: Criteria,
        pageable: Pageable
    ): Page<Book>

    fun incrementClicks(
        bookId: String
    ): Unit

    // Top 10 por clicks desde Mongo (criterio populares: no eliminados, dueño no READER).
    // Es el fallback cuando el cache por-libro no tiene los ids del ranking del ZSET.
    fun findTop10ByOrderByBookClicksDesc(): List<Book>

    // Total del catálogo de populares (para la paginación de la página 0 del Home).
    fun countPopularBooks(): Long
}
