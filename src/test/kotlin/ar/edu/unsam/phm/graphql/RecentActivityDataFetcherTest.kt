package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.domain.Common
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.OwnerDTO
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RecentActivityDataFetcherTest {

    private val bookRepository = mockk<MongoBookRepository>()
    private val reservationRepository = mockk<CrudReservationRepository>()
    private val fetcher = RecentActivityDataFetcher(bookRepository, reservationRepository)

    private fun book(title: String, owner: String, registeredAt: LocalDateTime) =
        Common().apply {
            this.title = title
            this.owner = OwnerDTO(name = owner)
            this.registeredAt = registeredAt
        }

    private fun reservation(title: String, reader: String, createdAt: LocalDateTime) =
        Reservation(
            user = User(name = reader),
            bookTitle = title,
            createdAt = createdAt,
        )

    @Test
    fun `unifica altas y reservas en una sola lista heterogénea`() {
        every { bookRepository.findTop5ByDeletedFalseOrderByRegisteredAtDesc() } returns
            listOf(book("Rayuela", "Ana", LocalDateTime.of(2026, 6, 10, 0, 0)))
        every { reservationRepository.findTop5ByOrderByCreatedAtDesc() } returns
            listOf(reservation("El Aleph", "Beto", LocalDateTime.of(2026, 6, 9, 10, 0)))

        val feed = fetcher.recentActivity()

        assertThat(feed).hasSize(2)
        assertThat(feed.map { it::class }).containsExactly(NewBookEvent::class, NewReservationEvent::class)
    }

    @Test
    fun `ordena cronológicamente descendente mezclando ambas fuentes`() {
        every { bookRepository.findTop5ByDeletedFalseOrderByRegisteredAtDesc() } returns listOf(
            book("Vieja", "Ana", LocalDateTime.of(2026, 1, 1, 0, 0)),
            book("Nueva", "Ana", LocalDateTime.of(2026, 6, 11, 0, 0)),
        )
        every { reservationRepository.findTop5ByOrderByCreatedAtDesc() } returns listOf(
            reservation("Media", "Beto", LocalDateTime.of(2026, 3, 1, 0, 0)),
        )

        val feed = fetcher.recentActivity()

        assertThat(feed.map { it.bookTitle }).containsExactly("Nueva", "Media", "Vieja")
    }

    @Test
    fun `recorta al top 5 cuando hay más eventos`() {
        every { bookRepository.findTop5ByDeletedFalseOrderByRegisteredAtDesc() } returns
            (1..5).map { book("B$it", "Ana", LocalDateTime.of(2026, 6, it, 0, 0)) }
        every { reservationRepository.findTop5ByOrderByCreatedAtDesc() } returns
            (1..5).map { reservation("R$it", "Beto", LocalDateTime.of(2026, 5, it, 0, 0)) }

        val feed = fetcher.recentActivity()

        // Los 5 más nuevos son todos altas de junio; ninguna reserva de mayo entra.
        assertThat(feed).hasSize(5)
        assertThat(feed).allMatch { it is NewBookEvent }
    }

    @Test
    fun `mapea usuario y título según el tipo de evento`() {
        every { bookRepository.findTop5ByDeletedFalseOrderByRegisteredAtDesc() } returns
            listOf(book("Rayuela", "Ana", LocalDateTime.of(2026, 6, 10, 0, 0)))
        every { reservationRepository.findTop5ByOrderByCreatedAtDesc() } returns
            listOf(reservation("El Aleph", "Beto", LocalDateTime.of(2026, 6, 9, 10, 0)))

        val feed = fetcher.recentActivity()

        val alta = feed.filterIsInstance<NewBookEvent>().single()
        assertThat(alta.user).isEqualTo("Ana")
        assertThat(alta.typeEvent).isEqualTo(ActivityEventType.BOOK_REGISTERED)

        val reserva = feed.filterIsInstance<NewReservationEvent>().single()
        assertThat(reserva.user).isEqualTo("Beto")
        assertThat(reserva.typeEvent).isEqualTo(ActivityEventType.RESERVATION)
    }
}
