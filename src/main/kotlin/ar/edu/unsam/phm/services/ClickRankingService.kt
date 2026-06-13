package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.repository.MongoBookRepository
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Service

/**
 * Ranking de libros más clickeados, vivo, sobre un SORTED SET de Redis.
 *   key:    "books:clicks"
 *   member: bookId (UUID)   ← el mismo id con el que se cruza Postgres (Reservation.bookId)
 *   score:  cantidad de clicks (histórico)
 *
 * Se actualiza con ZINCRBY en cada click.
 */
@Service
class ClickRankingService(
    val redisTemplate: StringRedisTemplate,
    val bookRepository: MongoBookRepository,
) {
    companion object {
        const val CLICKS_ZSET_KEY = "books-ranking:clicks"
    }

    // +1 al score del libro. ZINCRBY crea el member si no existía. O(log N), sub-ms.
    fun registerClick(bookId: String) {
        redisTemplate.opsForZSet().incrementScore(CLICKS_ZSET_KEY, bookId, 1.0)
    }

    // Top N ids por clicks (descendente). ZREVRANGE 0..n-1
    fun topBookIds(n: Long): List<String> =
        redisTemplate.opsForZSet().reverseRange(CLICKS_ZSET_KEY, 0, n - 1)?.toList() ?: emptyList()

    // Top N con su cantidad de clicks (para la métrica de GraphQL). ZREVRANGE ... WITHSCORES
    fun topWithClicks(n: Long): List<Pair<String, Long>> =
        redisTemplate.opsForZSet().reverseRangeWithScores(CLICKS_ZSET_KEY, 0, n - 1)
            ?.mapNotNull { tuple -> tuple.value?.let { it to (tuple.score?.toLong() ?: 0L) } }
            ?: emptyList()

    // Si el ZSET está vacío (Redis recién levantado), lo siembra con los bookClicks que ya
    // hay en Mongo. Solo los libros con clicks > 0 (el resto entra cuando reciba su primer
    // click). Lo invoca el ProjectBootstrap como un paso más de inicialización.
    fun seedFromMongoIfEmpty() {
        val size = redisTemplate.opsForZSet().size(CLICKS_ZSET_KEY) ?: 0
        if (size > 0) return

        val clickedBooks = bookRepository.findByBookClicksGreaterThan(0)
        if (clickedBooks.isEmpty()) return

        val tuples: Set<TypedTuple<String>> = clickedBooks
            .map { DefaultTypedTuple(it.bookId, it.bookClicks.toDouble()) }
            .toSet()
        redisTemplate.opsForZSet().add(CLICKS_ZSET_KEY, tuples) // ZADD masivo
        println("ZSET '$CLICKS_ZSET_KEY' sembrado con ${tuples.size} libros desde Mongo")
    }

    // Reconstruye el ZSET de cero desde Mongo: borra el set actual (puede tener bookIds
    // de un dataset anterior, que ya no existen en Mongo y dejan el ranking desincronizado)
    // y lo re-siembra con los bookClicks vigentes. Lo invoca el bootstrap, que es la fuente
    // autoritativa del baseline de popularidad.
    fun reseedFromMongo() {
        redisTemplate.delete(CLICKS_ZSET_KEY)
        seedFromMongoIfEmpty()
    }
}
