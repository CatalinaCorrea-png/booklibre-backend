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

    // Feeder del caché: el Top global (12) por clicks. Lo usa el job @Scheduled.
    fun findTop10ByOrderByBookClicksDesc(): Page<Book>

    // Fallback paginado del ranking populares (mismo criterio/orden que el caché),
    // para las páginas que ya no entran en Redis.
    fun findPopularBooks(pageable: Pageable): List<Book>

    // Total del ranking populares (para la paginación).
    fun countPopularBooks(): Long
}
