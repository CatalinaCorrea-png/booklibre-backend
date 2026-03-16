package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.services.BookService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class BookController(
    val bookService: BookService,
) {

    @PostMapping("/filtered-books")
    fun getFilteredBooks(@RequestBody bookSearchCriteria: BookSearchCriteria): PageResponse<BookDTO> {
        return bookService.getAvailableBooksBy(bookSearchCriteria)
    }

}