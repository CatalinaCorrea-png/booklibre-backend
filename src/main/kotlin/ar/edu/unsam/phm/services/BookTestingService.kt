//package ar.edu.unsam.phm.services
//
//import ar.edu.unsam.phm.domain.BookTesting
//import ar.edu.unsam.phm.errors.NotFoundException
//import ar.edu.unsam.phm.repository.MongoBookRepository
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//
//@Service
//class BookTestingService {
//
//    @Autowired
//    lateinit var mongoBookRepository: MongoBookRepository
//
//    @Transactional(readOnly = true)
//        fun getBookById(id: String): BookTesting =
//        mongoBookRepository.findById(id)
//            .orElseThrow{ NotFoundException ("No esta el libro con el id $id") }
//
//    @Transactional(readOnly = true)
//    fun getBookByTitulo(titulo: String): BookTesting? =
//        mongoBookRepository.findByTitulo(titulo) ?: throw NotFoundException("No esta el libro con el titulo $titulo")
////            .orElseThrow{ NotFoundException ("No esta el libro con el titulo $titulo") }
//}