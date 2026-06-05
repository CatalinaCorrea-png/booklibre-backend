package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.BookClick
import ar.edu.unsam.phm.domain.ClickDeduplication
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookClickRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
import ar.edu.unsam.phm.repository.MongoClickDeduplicationRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BookClickService(
    @Autowired
    val bookClickRepository: BookClickRepository,
    @Autowired
    val bookRepository: MongoBookRepository,
    @Autowired
    val userRepository: CrudUserRepository,
    @Autowired
    val clickDedupRepository: MongoClickDeduplicationRepository,
    @Autowired
    val clickRankingService: ClickRankingService,
) {
    fun registerClick(userId: String, bookId: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("No se encuentra un usuario registrado con el id: $userId") }
        val book = bookRepository.findById(bookId)
            .orElseThrow { NotFoundException("No se encuentra un libro registrado con el id: $bookId") }

        try {
            clickDedupRepository.save(ClickDeduplication(userId = userId, bookId = bookId))
        } catch (e: DuplicateKeyException) {
            return // already clicked
        }

        val bookClick = BookClick(bookId = bookId).apply {
            this.username = user.name
        }
        bookClickRepository.save(bookClick)
        bookRepository.incrementClicks(bookId)
        // +1 al ranking vivo en Redis. Usamos book.bookId (UUID), para poder
        // cruzar después con Postgres (Reservation.bookId) en la métrica de GraphQL.
        clickRankingService.registerClick(book.bookId)
    }
}
