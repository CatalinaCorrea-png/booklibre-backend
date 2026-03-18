package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookCreateDTO
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.createFromDTO
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.services.BookService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class BookController(
    val bookService: BookService,
    private val reservationRepository: ReservationRepository,
    private val bookRepository: BookRepository,
) {

    @PostMapping("/filtered-books")
    fun getFilteredBooks(@RequestBody bookSearchCriteria: BookSearchCriteria): PageResponse<BookDTO> {
        return bookService.getAvailableBooksBy(bookSearchCriteria)
    }

    @PostMapping("/crear-libro")
    fun crearLibro(@RequestBody bookCreateDTO: BookCreateDTO) {
        println("ownerId recibido: ${bookCreateDTO.ownerId}")
        val owner = bookService.getUser(bookCreateDTO.ownerId)
        val libroNuevo = bookCreateDTO.createFromDTO(owner)
        bookService.createBook(libroNuevo)
    }

}