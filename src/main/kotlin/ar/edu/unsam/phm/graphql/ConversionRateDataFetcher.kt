package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
import ar.edu.unsam.phm.services.ClickRankingService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

/**
 * KPI "Tasa de conversión": cruza el Top 5 de libros más clickeados (Redis) con
 * sus reservas (Postgres). El título se enriquece desde Mongo (un libro muy
 * clickeado puede no tener reservas, así que su título no siempre está en Postgres).
 */
@DgsComponent
class ConversionRateDataFetcher(
    val clickRankingService: ClickRankingService,
    val reservationRepository: CrudReservationRepository,
    val bookRepository: MongoBookRepository,
) {
    companion object {
        const val TOP_N = 5L
    }

    @DgsQuery
    fun conversionRate(): List<BookConversion> {
        // 1. Redis: top 5 ids + clicks  (ZREVRANGE books:clicks 0 4 WITHSCORES)
        val top5 = clickRankingService.topWithClicks(TOP_N)
        val bookIds = top5.map { it.first }
        if (bookIds.isEmpty()) return emptyList()

        // 2. Postgres: reservas de esos 5, en UNA sola query agrupada (sin N+1)
        val reservasByBook = reservationRepository.countByBookIds(bookIds)
            .associate { (it[0] as String) to (it[1] as Long) }

        // 3. Mongo: títulos de los 5, en una query
        val titleByBook = bookRepository.findAllByBookIdIn(bookIds)
            .associate { it.bookId to it.title }

        // 4. Cruzar y calcular la tasa = reservas / clicks
        return top5.map { (bookId, clicks) ->
            val reservas = reservasByBook[bookId] ?: 0L
            BookConversion(
                bookId = bookId,
                title = titleByBook[bookId] ?: "",
                clicks = clicks.toInt(),
                reservations = reservas.toInt(),
                conversionRate = if (clicks == 0L) 0.0 else reservas.toDouble() / clicks,
            )
        }
    }
}
