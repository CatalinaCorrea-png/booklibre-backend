package ar.edu.unsam.phm.controller


import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import org.springframework.web.bind.annotation.*


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
    fun createBook(@RequestBody bookCreateDTO: BookCreateDTO) {
        val owner = bookService.getUser(bookCreateDTO.ownerId)
        val newBook = bookCreateDTO.createFromDTO(owner)
        bookService.createBook(newBook)
    }

    @PutMapping("/editar-libro/{id}")
    fun editBook(@PathVariable id: Int, @RequestBody bookCreateDTO: BookCreateDTO) {
        val owner = bookService.getUser(bookCreateDTO.ownerId)
        val updatedBook = bookCreateDTO.createFromDTO(owner)
        bookService.updateBook(id, updatedBook)
    }

    @GetMapping("/book-detail/{id}")
    fun getBookById(@PathVariable id: Int) =
        bookService.getBookById(id).toDTO()

}