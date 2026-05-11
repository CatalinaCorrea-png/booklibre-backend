package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.BookClick
import org.springframework.data.mongodb.repository.MongoRepository

interface BookClickRepository : MongoRepository<BookClick, String> {
    fun countByBookId(bookId: String): Long

    // Para el bulk: un aggregation que devuelve {bookId, count} para N libros
    // Conviene el aggregation en bulk para no hacer 6 queries separadas
    //  — pero countByBookId también funciona bien para 6 registros, la diferencia es mínima.
}