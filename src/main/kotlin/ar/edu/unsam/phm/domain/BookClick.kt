package ar.edu.unsam.phm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

// log de eventos
@Document(collection = "book_clicks")
class BookClick(
    val bookId: String, // FK lógica al Book
) {
    @Id var id: String? = null
    lateinit var username: String
    var timestamp: LocalDateTime = LocalDateTime.now()

    // Podríamos agregar el titulo del libro desnormalizado. Pero para lo que usamos esto, podemos dejar el id solo
    // lateinit var bookTitle: String   // desnormalizado
}
//Con esto pueden hacer queries como "top 10 libros más clickeados del mes", "qué género tiene más visitas", "qué usuarios exploran más" — todo sin tocar PostgreSQL.