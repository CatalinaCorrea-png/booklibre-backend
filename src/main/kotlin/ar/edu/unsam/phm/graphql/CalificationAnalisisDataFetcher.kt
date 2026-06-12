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
        bookRepository.avgRatingByBookType().map {
            BookCalification(
                bookType = it.id,
                avgRating = Math.round(it.avgRating * 100) / 100.0,
            )
        }
}