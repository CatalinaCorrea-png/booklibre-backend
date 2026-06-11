package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.repository.MongoBookRepository
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

@DgsComponent
class CalificationAnalisisDataFetcher(
    val bookRepository: MongoBookRepository,
) {

    @DgsQuery
    fun calificactionAnalisis(): List<BookCalification> =
        bookRepository.findAll()
            .filter { !it.deleted && it.ratingAvg > 0.0 }
            .groupBy { it.bookType }
            .map { (bookType, books) ->
                BookCalification(
                    bookType = bookType,
                    avgRating = Math.round(books.map { it.ratingAvg }.average() * 100) / 100.0,
                )
            }
}