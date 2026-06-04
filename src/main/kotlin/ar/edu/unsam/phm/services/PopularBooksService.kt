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
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class PopularBooksService(
    val bookRepository: MongoBookRepository,
    val userRepository: CrudUserRepository,
    val redisTemplate: StringRedisTemplate,
    val objectMapper: ObjectMapper,
) {
    companion object {
        const val BOOKS_KEY = "home:popular-books"   // JSON con los 12 libros del Top
        const val TOTAL_KEY = "home:popular-total"   // total del ranking populares (para paginar)
        const val CACHED_SIZE = 12                   // "Top 10" del equipo, 12 reales (2 páginas de 6)
        const val REFRESH_RATE_MS = 5 * 60 * 1000L   // 5 minutos
    }

    // Refresca el Top cada 5 min. Es el ÚNICO punto que paga el costo en Mongo
    // (sort + count sobre los 500k); el Home nunca dispara esas queries.
    // fixedRate => corre al arrancar y luego cada 5 min.
    @Scheduled(fixedRate = REFRESH_RATE_MS)
    fun refreshPopularBooks() {
        val topBooks = bookRepository.findTop10ByOrderByBookClicksDesc().content
        redisTemplate.opsForValue().set(BOOKS_KEY, objectMapper.writeValueAsString(topBooks))
        redisTemplate.opsForValue().set(TOTAL_KEY, bookRepository.countPopularBooks().toString())
    }

    // Sirve una página del ranking populares del Home:
    //   - si la página entra en el Top cacheado (primeras 2 con pageSize 6) → Redis
    //   - si no (3ª en adelante, o aún sin caché) → Mongo, con la MISMA query global del caché
    // Como ambos ordenan igual (clicks DESC, mismo criterio), la paginación no salta en el borde.
    // Sobre los libros resultantes recalcula los bibliokarmas del usuario+fechas (barato).
    fun getPopularPage(criteria: BookSearchCriteria, pageable: Pageable): PageResponse<BookDTO> {
        val books = booksForPage(pageable)
        val total = readTotalFromCache() ?: bookRepository.countPopularBooks()
        return PageResponse(
            content = withBibliokarmas(books, criteria),
            page = pageable.pageNumber,
            pageSize = pageable.pageSize,
            totalElements = total.toInt(),
            totalPages = ceil(total.toDouble() / pageable.pageSize).toInt(),
        )
    }

    private fun booksForPage(pageable: Pageable): List<Book> {
        // ¿la página entra completa en [0, CACHED_SIZE)? Ej (pageSize 6): page0=[0,6) ✓ page1=[6,12) ✓ page2 ✗
        val fitsInCache = pageable.offset + pageable.pageSize <= CACHED_SIZE
        if (fitsInCache) {
            val cached = readBooksFromCache()
            val from = pageable.offset.toInt()
            if (cached != null && from < cached.size) {
                return cached.subList(from, minOf(from + pageable.pageSize, cached.size))
            }
        }
        // fuera del caché (o caché vacío en el primer arranque): Mongo, misma query global.
        return bookRepository.findPopularBooks(pageable)
    }

    private fun readBooksFromCache(): List<Book>? {
        val cached = redisTemplate.opsForValue().get(BOOKS_KEY) ?: return null
        val listType = objectMapper.typeFactory.constructCollectionType(List::class.java, Book::class.java)
        return objectMapper.readValue(cached, listType)
    }

    private fun readTotalFromCache(): Long? = redisTemplate.opsForValue().get(TOTAL_KEY)?.toLongOrNull()

    // Sobre los libros recalcula los bibliokarmas del usuario + fechas del criteria.
    // Barato: 1 lookup de user + un cálculo por libro de la página.
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
