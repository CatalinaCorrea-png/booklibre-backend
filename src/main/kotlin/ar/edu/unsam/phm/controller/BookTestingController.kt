//package ar.edu.unsam.phm.controller
//
//import ar.edu.unsam.phm.domain.BookTesting
//import ar.edu.unsam.phm.services.BookTestingService
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.web.bind.annotation.CrossOrigin
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@CrossOrigin(origins = ["*"])
//class BookTestingController {
//
//    @Autowired
//    lateinit var bookTestingService: BookTestingService
//
//    @GetMapping("/books/{id}")
//    fun getBookById(@PathVariable id: String): BookTesting =
//        this.bookTestingService.getBookById(id)
//
//    @GetMapping("/book-titulo/{titulo}")
//    fun getBookByTitulo(@PathVariable titulo: String): BookTesting? =
//        this.bookTestingService.getBookByTitulo(titulo)
//}