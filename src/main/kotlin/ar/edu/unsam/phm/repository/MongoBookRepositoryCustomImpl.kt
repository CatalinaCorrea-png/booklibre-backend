package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
}
