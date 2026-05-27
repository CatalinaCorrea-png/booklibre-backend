package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.BookClick
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookClickRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
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
) {
    fun registerClick(userId: String, bookId: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("No se encuentra un usuario registrado con el id: $userId") }
        val book = bookRepository.findById(bookId)
            .orElseThrow { NotFoundException("No se encuentra un libro registrado con el id: $bookId") }

        val bookClick = BookClick(bookId = bookId).apply {
            this.username = user.name
        }
        bookClickRepository.save(bookClick)
        bookRepository.incrementClicks(bookId)
    }
}
