package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.services.UserService
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil


@Service
class BookService(
    val bookRepository: BookRepository,
    val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
) {

    fun createBook(bookCreateDTO: BookCreateDTO){
        if (!userRepository.objectInCollection(bookCreateDTO.ownerId)) {
            throw NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}")
        }
        val owner = userRepository.getObject(bookCreateDTO.ownerId)
        val newBook = bookCreateDTO.createFromDTO(owner)
        newBook.meetsCreationCriteria()
        bookRepository.create(newBook)
    }

    fun updateBook(id: Int, bookCreateDTO: BookCreateDTO) {
        if (!userRepository.objectInCollection(bookCreateDTO.ownerId)) {
            throw NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}")
        }
        if (!bookRepository.objectInCollection(id)) {
            throw NotFoundException("No existe el libro con id: $id")
        }
        val owner = userRepository.getObject(bookCreateDTO.ownerId)
        val newBook = bookCreateDTO.createFromDTO(owner)
        val existingBook = bookRepository.getObject(id)
        newBook.id = existingBook.id
        newBook.meetsCreationCriteria()
        bookRepository.update(newBook)
        reservationRepository.updateBookReference(newBook)
    }

    fun deleteBook(bookId: Int) {
        reservationRepository.deleteAllReservationsByBookId(bookId)
        bookRepository.delete(bookId)
    }

    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable ): PageResponse<BookDTO> {
        val reservedBookIds : Set<Int> = reservationRepository.findReservedBookIds(searchCriteria)
        val page : Page<Book> = bookRepository.findAllByCriteria(searchCriteria, reservedBookIds, pageable)

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
        val user = userRepository.getObject(criteria.userId)
        val bookDTOs = books.map { book ->
            val bookDTO = book.toDTO()
            bookDTO.bookBibliokarmas = book.calculateBibliokarmas(reservationTemp.reservationDays(), user.bibliokarmas)
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

    fun getUser(id: Int): User {
        return userRepository.getObject(id)
    }

    fun getBookById(id: Int): Book =
        bookRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")

    fun recalculateBibliokarmas(bookId: Int, userId: Int, pickUpDate: LocalDate, dropOffDate: LocalDate): Int {
        val book = bookRepository.getObject(bookId)
        val user = userRepository.getObject(userId)
        val days = Reservation(pickUpDate = pickUpDate, dropOffDate = dropOffDate).reservationDays()
        return book.calculateBibliokarmas(days, user.bibliokarmas)
    }
}
