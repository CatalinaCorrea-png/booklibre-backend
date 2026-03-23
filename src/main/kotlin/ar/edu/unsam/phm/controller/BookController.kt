package ar.edu.unsam.phm.controller


import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Gender
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*


@RestController
@CrossOrigin("*")
class BookController(
    val bookService: BookService,
    private val reservationRepository: ReservationRepository,
    private val bookRepository: BookRepository,
) {

    @GetMapping("/filtered-books")
    fun getFilteredBooks(
        @ModelAttribute criteria: BookSearchCriteria,
//        @RequestParam(defaultValue = "0") page: Int,
//        @RequestParam(defaultValue = "6") size: Int,
//        @RequestParam(defaultValue = "title") sortBy: String,
//        @RequestParam(defaultValue = "true") ascending: Boolean
    ): PageResponse<BookDTO> {
//        println(criteria.toString())
//        println("$page, $size, $sortBy, $ascending")
        val direction = if (criteria.ascending) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(criteria.page, criteria.pageSize, Sort.by(direction, criteria.sortBy))
        return bookService.searchBooks(criteria, pageable)
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

    @DeleteMapping("/eliminar-libro/{id}")
    fun deleteBook(@PathVariable id: Int) {
        bookService.deleteBook(id)
    }

    @GetMapping("/book-detail/{id}")
    fun getBookById(@PathVariable id: Int) =
        bookService.getBookById(id).toDTO()

    @GetMapping("/book-genders")
    fun getBookGenders() = Gender.entries
}