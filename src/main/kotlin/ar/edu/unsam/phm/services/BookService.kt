package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.CrudBookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class BookService(
    val bookRepository: BookRepository,
    val bookRepository1: CrudBookRepository,
    val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
) {

    fun createBook(bookCreateDTO: BookCreateDTO){
        if (!userRepository.objectInCollection(bookCreateDTO.ownerId!!)) {
            throw NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}")
        }
        val owner = userRepository.getObject(bookCreateDTO.ownerId)
        val newBook = bookCreateDTO.createFromDTO(owner)
        newBook.validate()
        bookRepository.create(newBook)
    }

    fun updateBook(id: Long, bookCreateDTO: BookCreateDTO) {
        if (!userRepository.objectInCollection(bookCreateDTO.ownerId!!)) {
            throw NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}")
        }
        if (!bookRepository.objectInCollection(id)) {
            throw NotFoundException("No existe el libro con id: $id")
        }
        val owner = userRepository.getObject(bookCreateDTO.ownerId)
        val newBook = bookCreateDTO.createFromDTO(owner)
        val existingBook = bookRepository.getObject(id)
        newBook.id = existingBook.id
        newBook.validate()
        bookRepository.update(newBook)
        reservationRepository.updateBookReference(newBook)
    }

    fun deleteBook(bookId: Long) {
        reservationRepository.deleteAllReservationsByBookId(bookId)
        bookRepository.delete(bookId)
    }

    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable ): PageResponse<BookDTO> {
        val reservedBookIds : Set<Long> = reservationRepository.findReservedBookIds(searchCriteria)
        // no pasar ids al repo. Me traigo las dos listas y hago la dif aca (sacar las reservadas
        val filteredAndAvailable : List<Book> = bookRepository.findAllByCriteria(searchCriteria).filter { book -> book.id !in reservedBookIds } // no estÃ¡ reservado

        val page : Page<Book> = bookRepository.sortAndPage(filteredAndAvailable, pageable)

        val booksWithBibliokarmasDTO : List<BookDTO> = getBooksBibliokarmasDTO(page.content, searchCriteria)
        val booksWithRatings : List<BookDTO> = calculateRatingAvg(booksWithBibliokarmasDTO)
        return PageResponse(
            content = booksWithRatings,
            page = page.number,
            pageSize = page.size,
            totalElements = page.totalElements.toInt(),
            totalPages = page.totalPages
        )
    }

    fun getBooksBibliokarmasDTO(books: List<Book>, criteria: BookSearchCriteria) : List<BookDTO> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)
        val user = userRepository.getObject(criteria.userId!!)
        val bookDTOs = books.map { book ->
            val bookReservationsNumber = reservationRepository.findByBookId(book.id!!).size
            val bookDTO = book.toDTO()
            bookDTO.bookBibliokarmas = book.calculateBibliokarmas(reservationTemp.reservationDays(), user.bibliokarmas, bookReservationsNumber)
            bookDTO
        }
        return bookDTOs
    }

    fun calculateRatingAvg(booksDTO: List<BookDTO>) : List<BookDTO> {
        return booksDTO.map { bookDTO ->
            val ratings = reservationRepository.findRatingsByBookId(bookDTO.id).filter { !(it <= 0.0) }
            bookDTO.rating = if (ratings.isEmpty()) 0.0 else ratings.average()
            bookDTO
        }
    }

    fun getUser(id: Long): User {
        return userRepository.getObject(id)
    }

    fun getBookById(id: Long): Book = bookRepository1
        .findById(id)
        .orElseThrow {
            NotFoundException("No se encuentra un libro registrado con el id: $id")
        }

    fun recalculateBibliokarmas(bookId: Long, userId: Long, pickUpDate: LocalDate, dropOffDate: LocalDate): Int {
        val book = bookRepository.getObject(bookId)
        val user = userRepository.getObject(userId)
        val bookReservationsNumber = reservationRepository.findByBookId(bookId).size
        val days = Reservation(pickUpDate = pickUpDate, dropOffDate = dropOffDate).reservationDays()
        return book.calculateBibliokarmas(days, user.bibliokarmas, bookReservationsNumber)
    }
}