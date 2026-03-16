package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.services.BookService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class BookController(private val bookService: BookService) {
}