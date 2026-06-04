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

    fun findTop10ByOrderByBookClicksDesc(): Page<Book>
}
