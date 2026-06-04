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

    override fun findTop10ByOrderByBookClicksDesc(): Page<Book> {
        // "Top 10" por convención del equipo, pero traemos 12 (múltiplo de 6, el pageSize del Home).
        // Solo lo llama el job @Scheduled que refresca el caché; el Home nunca dispara esta query.
        val pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "bookClicks"))
        val books = mongoTemplate.find(Query(popularCriteria()).with(pageable), Book::class.java)
        return PageImpl(books, pageable, books.size.toLong())
    }

    // Fallback paginado: páginas que ya no entran en el caché (de la 3ª en adelante).
    // Usa EXACTAMENTE el mismo criterio y orden que el caché, así la paginación
    // Redis→Mongo no salta en el borde.
    override fun findPopularBooks(pageable: Pageable): List<Book> =
        mongoTemplate.find(Query(popularCriteria()).with(pageable), Book::class.java)

    // Total del ranking populares, para reportar la paginación. Lo calcula el job 1 vez
    // cada 5 min y se guarda en Redis; el Home no lo recalcula.
    override fun countPopularBooks(): Long =
        mongoTemplate.count(Query(popularCriteria()), Book::class.java)
}
