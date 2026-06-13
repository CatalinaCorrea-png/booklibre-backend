package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.UserTypes
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class MongoBookRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate
) : MongoBookRepositoryCustom {

    override fun findUserBooks(
        ownerId: String,
        filterCriteria: Criteria,
        pageable: Pageable
    ): Page<Book> {
        val ownerScope = Criteria.where("owner.id").`is`(ownerId).and("deleted").`is`(false)
        val combined = Criteria().andOperator(ownerScope, filterCriteria)
        val baseQuery = Query.query(combined)

        val total = mongoTemplate.count(baseQuery, Book::class.java)
        val books = mongoTemplate.find(baseQuery.with(pageable), Book::class.java)

        return PageImpl(books, pageable, total)
    }

    override fun findByCriteria(
        criteria: Criteria,
        pageable: Pageable
    ): Page<Book> {
        val query = Query(criteria).with(pageable)
        val books = mongoTemplate.find(query, Book::class.java)
        val total = mongoTemplate.count(Query(criteria), Book::class.java)

        return PageImpl(books, pageable, total)
    }

    override fun incrementClicks(bookId: String) {
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").`is`(bookId)),
            Update().inc("bookClicks", 1),
            Book::class.java
        )
    }

    // Criterio del ranking "populares" (global): no eliminados y cuyo dueño NO sea READER
    // (un lector no presta, así que su libro es como un deleted: no se puede reservar).
    // Lo comparten el feeder del caché, el fallback paginado y el count, para que las 3
    // vistas sean consistentes entre sí.
    private fun popularCriteria(): Criteria =
        Criteria.where("deleted").`is`(false)
            .and("owner.userType").ne(UserTypes.READER.name)

    override fun findTop10ByOrderByBookClicksDesc(): List<Book> {
        // Top 10 por clicks (criterio populares). Fallback del cache por-libro: trae 10 de
        // colchón aunque el Home use 6 (por si algunos se vencieron del cache por su TTL).
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookClicks"))
        return mongoTemplate.find(Query(popularCriteria()).with(pageable), Book::class.java)
    }

    // Cuenta los libros que matchean un criterio. La usa la página 0 del Home para que su total
    // sea consistente con findByCriteria (mismo criterio per-usuario), evitando el desfasaje de
    // páginas entre la página 0 (antes contaba el catálogo global) y las siguientes.
    override fun countByCriteria(criteria: Criteria): Long =
        mongoTemplate.count(Query(criteria), Book::class.java)
}
