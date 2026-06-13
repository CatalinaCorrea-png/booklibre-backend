package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional
import ar.edu.unsam.phm.graphql.CatalogHealth
import java.time.LocalDate

interface MongoBookRepository : MongoRepository<Book, String>, MongoBookRepositoryCustom {
    fun findByTitle(title: String): Optional<Book>
    fun findFirstByTitle(title: String): Optional<Book>
    fun findAllByTitle(title: String): List<Book>
    fun findByIsbn(isbn: String): MutableList<Book>
    fun findByBookId(bookId: String): Optional<Book>
    fun findAllByBookIdIn(bookIds: List<String>): List<Book>
    fun findAllByOwnerId(ownerId: String): List<Book>

    // Libros que tienen al menos un click. Para sembrar el ZSET de ranking al arrancar.
    fun findByBookClicksGreaterThan(clicks: Int): List<Book>
    fun findByDeletedFalse(): MutableList<Book>

    @Aggregation(pipeline = [
        "{ \$match: { deleted: { \$ne: true } } }",

        "{ \$addFields: { bucket: { \$switch: { branches: [ " +
                "{ case: { \$eq: [ { \$size: { \$ifNull: [ '\$reservations', [] ] } }, 0 ] }, then: 'NUNCA_RESERVADO' }, " +
                "{ case: { \$gt: [ { \$size: { \$filter: { input: { \$ifNull: [ '\$reservations', [] ] }, as: 'r', cond: { \$and: [ { \$lte: [ '\$\$r.pickUpDate', ?0 ] }, { \$gte: [ '\$\$r.dropOffDate', ?0 ] } ] } } } }, 0 ] }, then: 'PRESTADO' }, " +
                "{ case: { \$gt: [ { \$size: { \$filter: { input: { \$ifNull: [ '\$reservations', [] ] }, as: 'r', cond: { \$lt: [ '\$\$r.dropOffDate', ?0 ] } } } }, 0 ] }, then: 'DEVUELTO' } " +
                "], default: 'RESERVADO_A_FUTURO' } } } }",

        "{ \$group: { _id: null, total: { \$sum: 1 }, " +
                "prestados:                    { \$sum: { \$cond: [ { \$eq: [ '\$bucket', 'PRESTADO' ] },          1, 0 ] } }, " +
                "disponiblesNuncaReservados:   { \$sum: { \$cond: [ { \$eq: [ '\$bucket', 'NUNCA_RESERVADO' ] },    1, 0 ] } }, " +
                "disponiblesReservadosAFuturo: { \$sum: { \$cond: [ { \$eq: [ '\$bucket', 'RESERVADO_A_FUTURO' ] }, 1, 0 ] } }, " +
                "disponiblesDevueltos:         { \$sum: { \$cond: [ { \$eq: [ '\$bucket', 'DEVUELTO' ] },           1, 0 ] } } } }"
    ])
    fun catalogHealth(today: LocalDate): CatalogHealth?

    @Aggregation(pipeline = [
        "{ \$match: { deleted: false, ratingAvg: { \$gt: 0.0 } } }",
        "{ \$group: { _id: '\$bookType', avgRating: { \$avg: '\$ratingAvg' } } }"
    ])
    fun avgRatingByBookType(): List<BookTypeAvgResult>


data class BookTypeAvgResult(val id: String, val avgRating: Double)
    // Top 5 de libros dados de alta más recientemente (no eliminados). Feed de actividad:
    // ordena por registeredAt (con hora) para precisión intra-día.
    fun findTop5ByDeletedFalseOrderByRegisteredAtDesc(): List<Book>

}
