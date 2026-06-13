package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Cache por-libro en Redis: una clave "book:<bookId>" por libro, con TTL.
 * Lo usan tanto el Home de populares (PopularBooksService) como las búsquedas
 * paginadas (BookService.searchBooks), para ir calentando el cache a medida que
 * se navegan libros. Los inactivos se vencen solos por el TTL.
 */
@Service
class BookCacheService(
    val redisTemplate: StringRedisTemplate,
    val objectMapper: ObjectMapper,
) {
    companion object {
        const val BOOK_CACHE_PREFIX = "cached-books:"
        val BOOK_TTL: Duration = Duration.ofMinutes(5) // TTL 5 minutos
    }

    fun cacheBook(book: Book) {
        redisTemplate.opsForValue()
            .set(BOOK_CACHE_PREFIX + book.bookId, objectMapper.writeValueAsString(book), BOOK_TTL)
    }

    fun cacheBooks(books: List<Book>) = books.forEach { cacheBook(it) }

    // Lee varios libros del cache de una (MGET). Devuelve bookId -> Book? (null si no está).
    fun readBooks(ids: List<String>): Map<String, Book?> {
        val keys = ids.map { BOOK_CACHE_PREFIX + it }
        val jsons = redisTemplate.opsForValue().multiGet(keys) ?: emptyList()
        return ids.mapIndexed { i, id -> id to jsons.getOrNull(i)?.let { deserializeBook(it) } }.toMap()
    }

    private fun deserializeBook(json: String): Book = objectMapper.readValue(json, Book::class.java)
}
