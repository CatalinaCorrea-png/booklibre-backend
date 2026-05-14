package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Gender
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.BookClickService
import ar.edu.unsam.phm.services.BookService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
//@CrossOrigin("*")
class BookController(
    val bookService: BookService,
    val bookClickService: BookClickService,
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
    fun editBook(@PathVariable id: String, @RequestBody bookCreateDTO: BookCreateDTO) {
        bookService.updateBook(id, bookCreateDTO)
    }

    @DeleteMapping("/delete-book/{id}")
    fun deleteBook(@PathVariable id: String) {
        bookService.deleteBook(id)
    }

    @GetMapping("/book-detail/{id}")
    fun getBookById(@PathVariable id: String, @RequestParam userId: String) : BookDTO =
        bookService.getBookById(id)

    @PostMapping("/book-detail/{id}/click")
    fun registerBookClick(@PathVariable id: String, @RequestParam userId: String) {
        // Si el click falla, no debería afectar la lectura del libro
        bookClickService.registerClick(userId, id)
    }

    @GetMapping("/userOwnBooks/{userId}")
    fun getAllUserBooks(
        @PathVariable userId: String,
        @ModelAttribute pageableObject: ProfileBookPageable
    ): PagedResult<ProfileBookDTO> {
        return bookService.getAllUserBooks(userId, pageableObject)
    }

    @GetMapping("/book-detail/{id}/bibliokarmas")
    fun calculateBibliokarmas(
        @PathVariable id: String,
        @RequestParam userId: String,
        @RequestParam pickUpDate: LocalDate,
        @RequestParam dropOffDate: LocalDate
    ): Long =
        bookService.recalculateBibliokarmas(id, userId, pickUpDate, dropOffDate)

    @GetMapping("/book-genders")
    fun getBookGenders() = Gender.entries

    @GetMapping("/book-review/{bookId}")
    fun getBookReviews(
        @PathVariable bookId: String,
        @RequestParam page: Int,
        @RequestParam pageSize: Int
    ): List<ReviewDTO> =
        bookService.getBookReviews(bookId, page, pageSize).map { it.toDTO() }
}