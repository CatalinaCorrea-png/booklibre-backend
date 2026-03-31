package ar.edu.unsam.phm.controller


import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Gender
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.BookService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@RestController
@CrossOrigin("*")
class BookController(
    val bookService: BookService,
) {
    @GetMapping("/filtered-books")
    fun getFilteredBooks(
        @ModelAttribute criteria: BookSearchCriteria,

    ): PageResponse<BookDTO> {
        val direction = if (criteria.ascending) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(criteria.page, criteria.pageSize, Sort.by(direction, criteria.sortBy))
        return bookService.searchBooks(criteria, pageable)
    }

    @PostMapping("/create-book")
    fun createBook(@RequestBody bookCreateDTO: BookCreateDTO) {
        bookService.createBook(bookCreateDTO)
    }

    @PutMapping("/edit-book/{id}")
    fun editBook(@PathVariable id: Long, @RequestBody bookCreateDTO: BookCreateDTO) {
        bookService.updateBook(id, bookCreateDTO)
    }

    @DeleteMapping("/delete-book/{id}")
    fun deleteBook(@PathVariable id: Long) {
        bookService.deleteBook(id)
    }

    @GetMapping("/book-detail/{id}")
    fun getBookById(@PathVariable id: Long) =
        bookService.getBookById(id).toDTO()

    @GetMapping("/book-detail/{id}/bibliokarmas")
    fun calculateBibliokarmas(@PathVariable id: Long, @RequestParam userId: Long, @RequestParam pickUpDate: LocalDate, @RequestParam dropOffDate: LocalDate): Int =
        bookService.recalculateBibliokarmas(id, userId, pickUpDate, dropOffDate)


    @GetMapping("/book-genders")
    fun getBookGenders() = Gender.entries
}