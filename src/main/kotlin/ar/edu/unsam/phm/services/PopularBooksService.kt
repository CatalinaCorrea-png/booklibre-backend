package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
import ar.edu.unsam.phm.specification.BookSpecifications
import org.springframework.stereotype.Service
import kotlin.math.ceil

/**
 * Arma la primera página del Home (los más populares por clicks) leyendo de Redis:
 *   1. ranking de ids desde el ZSET (ClickRankingService)
 *   2. datos de cada libro desde el cache por-libro (BookCacheService)
 *   3. si faltan, los trae del Top10PorClicks de Mongo y los cachea
 * Sobre los 6 primeros calcula los bibliokarmas del usuario+fechas.
 */
@Service
class PopularBooksService(
    val bookRepository: MongoBookRepository,
    val userRepository: CrudUserRepository,
    val clickRankingService: ClickRankingService,
    val bookCacheService: BookCacheService,
) {
    companion object {
        const val HOME_PAGE_SIZE = 6                     // lo que muestra el Home
        const val RANKING_FETCH = 10L                    // top 10 del ZSET (colchón); usamos 6
    }

    // Página 0 del Home: populares por clicks, desde Redis.
    fun getPopularFirstPage(criteria: BookSearchCriteria): PageResponse<BookDTO> {
        val books = topPopularBooks()
        // El total se cuenta con EL MISMO criterio per-usuario que usan las páginas 1+
        // (searchBooks → byCriteriaMongo): excluye los libros propios y los reservados en
        // esas fechas. Así la cantidad de páginas es consistente entre la página 0 y el resto.
        val total = bookRepository.countByCriteria(BookSpecifications.byCriteriaMongo(criteria))
        return PageResponse(
            content = withBibliokarmas(books, criteria),
            page = 0,
            pageSize = HOME_PAGE_SIZE,
            totalElements = total.toInt(),
            totalPages = ceil(total.toDouble() / HOME_PAGE_SIZE).toInt(),
        )
    }

    // Devuelve hasta 6 Books en orden de ranking, resolviendo el cache por-libro
    // y cayendo a Mongo (Top10PorClicks) solo si falta alguno.
    private fun topPopularBooks(): List<Book> {
        val ids = clickRankingService.topBookIds(RANKING_FETCH)
        if (ids.isEmpty()) return refillFromMongo().take(HOME_PAGE_SIZE) // ZSET vacío → Mongo

        val cached = bookCacheService.readBooks(ids)
        val resolved = if (ids.any { cached[it] == null }) {
            // Falta alguno en cache → traigo el Top 10 de Mongo y lo cacheo, después resuelvo.
            val refilled = refillFromMongo().associateBy { it.bookId }
            ids.mapNotNull { cached[it] ?: refilled[it] }
        } else {
            ids.mapNotNull { cached[it] }
        }
        return resolved.take(HOME_PAGE_SIZE)
    }

    // Top 10 por clicks de Mongo + lo deja cacheado por-libro (con TTL).
    private fun refillFromMongo(): List<Book> {
        val books = bookRepository.findTop10ByOrderByBookClicksDesc()
        bookCacheService.cacheBooks(books)
        return books
    }

    private fun withBibliokarmas(books: List<Book>, criteria: BookSearchCriteria): List<BookDTO> {
        val user = userRepository.findById(criteria.userId!!)
            .orElseThrow { NotFoundException("No existe user con id: ${criteria.userId}") }
        val reservationDays = Reservation(
            pickUpDate = criteria.pickUpDate,
            dropOffDate = criteria.dropOffDate
        ).reservationDays()
        return books.map { book ->
            book.toDTO().apply {
                bookBibliokarmas = book.calculateBibliokarmas(reservationDays, user.bibliokarmas)
            }
        }
    }
}
