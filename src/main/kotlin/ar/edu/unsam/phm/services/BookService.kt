package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*


@Service
class BookService(
    val bookRepository: CrudBookRepository,
    val reservationRepository: CrudReservationRepository,
    private val userRepository: CrudUserRepository,
    private val authorRepository: CrudAuthorRepository,
) {


    @Transactional
    fun createBook(bookCreateDTO: BookCreateDTO) {
        val owner = userRepository.findById(bookCreateDTO.ownerId!!)
            .orElseThrow { NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}") }

        val author: Author = authorRepository.findByName(bookCreateDTO.book.author.name)
            .orElseGet { authorRepository.save(Author(name = bookCreateDTO.book.author.name, avatar = bookCreateDTO.book.author.avatar)) }

        val newBook = bookCreateDTO.createFromDTO(owner)
        newBook.author = author
        newBook.validate()
        bookRepository.save(newBook)
    }

    @Transactional
    fun updateBook(id: Long, bookCreateDTO: BookCreateDTO) {
        val owner = userRepository.findById(bookCreateDTO.ownerId!!)
            .orElseThrow { NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}") }

        val existingBook = bookRepository.findById(id)
            .orElseThrow { NotFoundException("No existe el libro con id: $id") }

        if (existingBook.deleted) {
            throw ConflictException("No se puede modificar un libro eliminado")
        }

        val author: Author = authorRepository.findByName(bookCreateDTO.book.author.name)
            .orElseGet { authorRepository.save(Author(name = bookCreateDTO.book.author.name, avatar = bookCreateDTO.book.author.avatar)) }

        val newBook = bookCreateDTO.createFromDTO(owner)
        newBook.id = existingBook.id
        newBook.author = author
        newBook.validate()
        bookRepository.save(newBook)
    }

    @Transactional
    fun deleteBook(bookId: Long) {
        val book = bookRepository.findById(bookId)
            .orElseThrow { NotFoundException("No existe el libro con id: $bookId") }

        val reservations = reservationRepository.findByBookId(bookId)

        if (reservations.any { it.state == State.BORROWED }) {
            throw ConflictException("No se puede eliminar un libro que está prestado")
        }

        reservations
            .filter { it.state == State.RESERVED }
            .forEach { reservationRepository.delete(it) }

        book.logicDelete()
        bookRepository.save(book)  // guarda el libro con el delete logico, no lo borra de la coleccion
    }



//    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable ): PageResponse<BookDTO> {
//        val reservedBookIds : Set<Long> = reservationRepository.findReservedBookIds(searchCriteria)
//        // no pasar ids al repo. Me traigo las dos listas y hago la dif aca (sacar las reservadas
//        val filteredAndAvailable : List<Book> = bookRepository.findAllByCriteria(searchCriteria).filter { book -> book.id !in reservedBookIds } // no estÃ¡ reservado
//
//        val page : Page<Book> = bookRepository.sortAndPage(filteredAndAvailable, pageable)
//
//        val booksWithBibliokarmasDTO : List<BookDTO> = getBooksBibliokarmasDTO(page.content, searchCriteria)
//        val booksWithRatings : List<BookDTO> = calculateRatingAvg(booksWithBibliokarmasDTO)
//        return PageResponse(
//            content = booksWithRatings,
//            page = page.number,
//            pageSize = page.size,
//            totalElements = page.totalElements.toInt(),
//            totalPages = page.totalPages
//        )
//    }
//
//    fun getBooksBibliokarmasDTO(books: List<Book>, criteria: BookSearchCriteria) : List<BookDTO> {
//        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)
//        val user = userRepository.getObject(criteria.userId!!)
//        val bookDTOs = books.map { book ->
//            val bookReservationsNumber = reservationRepository.findByBookId(book.id!!).size
//            val bookDTO = book.toDTO()
//            bookDTO.bookBibliokarmas = book.calculateBibliokarmas(reservationTemp.reservationDays(), user.bibliokarmas, bookReservationsNumber)
//            bookDTO
//        }
//        return bookDTOs
//    }
//
//    fun calculateRatingAvg(booksDTO: List<BookDTO>) : List<BookDTO> {
//        return booksDTO.map { bookDTO ->
//            val ratings = reservationRepository.findRatingsByBookId(bookDTO.id).filter { !(it <= 0.0) }
//            bookDTO.rating = if (ratings.isEmpty()) 0.0 else ratings.average()
//            bookDTO
//        }
//    }
//
//    fun getUser(id: Long): User {
//        return userRepository.getObject(id)
//    }
//
    fun getBookById(id: Long): Optional<Book> =
        bookRepository.findById(id) ?: throw NotFoundException("Can not find the book <$id>")

//trae todos los libros menos los que fueron eliminados logicamente IMPORTANTE USAR ESTE METODO SINO VA A TRAER LIBROS QUE FUERON BORRADOS LOGICAMENTEEEE
    fun getAllBooks(): List<Book> = bookRepository.findAllByDeletedIsFalse()

//    fun recalculateBibliokarmas(bookId: Long, userId: Long, pickUpDate: LocalDate, dropOffDate: LocalDate): Int {
//        val book = bookRepository.getObject(bookId)
//        val user = userRepository.getObject(userId)
//        val bookReservationsNumber = reservationRepository.findByBookId(bookId).size
//        val days = Reservation(pickUpDate = pickUpDate, dropOffDate = dropOffDate).reservationDays()
//        return book.calculateBibliokarmas(days, user.bibliokarmas, bookReservationsNumber)
//    }
}