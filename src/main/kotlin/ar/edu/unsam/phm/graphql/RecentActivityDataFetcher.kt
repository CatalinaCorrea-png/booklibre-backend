package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * KPI "Actividad reciente": unifica los últimos libros dados de alta (Mongo, Book.registeredAt)
 * y las últimas reservas confirmadas (Postgres, Reservation.createdAt) en una lista
 * heterogénea, orden cronológico descendente, top 5.
 */
@DgsComponent
class RecentActivityDataFetcher(
    val bookRepository: MongoBookRepository,
    val reservationRepository: CrudReservationRepository,
) {
    companion object { const val FEED_SIZE = 5 }

    // Serializa el instante CON offset de zona (ej. "2026-06-11T20:00:00-03:00"). Un
    // LocalDateTime.toString() no lleva zona y el navegador lo interpretaría con SU zona
    // horaria → fecha corrida. Con el offset, new Date() del front resuelve el instante exacto.
    private fun LocalDateTime.toIsoWithOffset(): String =
        this.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString()

    @DgsQuery
    fun recentActivity(): List<ActivityEvent> {
        // Cada lado trae a lo sumo 5: los 5 más nuevos del feed salen de la unión de ambos.
        // Guardo un LocalDateTime real como clave de orden para no ordenar por String.
        val bookEvents = bookRepository.findTop5ByDeletedFalseOrderByRegisteredAtDesc()
            .map { book ->
                // Fallback para libros viejos sin registeredAt: usamos su fecha de alta.
                val instant = book.registeredAt ?: book.createdAt.atStartOfDay()
                instant to NewBookEvent(
                    date = instant.toIsoWithOffset(),   // ISO con hora+offset → "recién" en el front
                    user = book.owner.name,   // owner que dio de alta el libro
                    bookTitle = book.title,
                )
            }

        val reservationEvents = reservationRepository.findTop5ByOrderByCreatedAtDesc()
            .map { res ->
                val date = res.createdAt // null-safe para filas legacy
                date to NewReservationEvent(
                    date = date.toIsoWithOffset(),
                    user = res.user.name,     // lector que reservó
                    bookTitle = res.bookTitle,
                )
            }

        // Orden cronológico desc, con desempate determinístico por título: si un libro y
        // una reserva comparten fecha, el orden no queda librado al que devuelva la base.
        return (bookEvents + reservationEvents)
            .sortedWith(compareByDescending<Pair<LocalDateTime, ActivityEvent>> { it.first }
                .thenBy { it.second.bookTitle })
            .take(FEED_SIZE)
            .map { it.second }
    }
}
