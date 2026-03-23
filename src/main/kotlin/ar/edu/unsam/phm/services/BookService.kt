package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import kotlin.math.ceil


@Service
class BookService(
    val bookRepository: BookRepository,
    val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
) {
    fun createBook(book: Book){
        book.meetsCreationCriteria()
        bookRepository.create(book)
    }

    fun updateBook(id: Int, book: Book) {
        val existingBook = bookRepository.getObject(id)
        book.id = existingBook.id
        book.meetsCreationCriteria()
        bookRepository.update(book)
    }

    fun deleteBook(id: Int) {
        println("Buscando reservas para libro ID: $id")
        val activeReservations = reservationRepository.collection
            .filter { println("Reserva: book.id=${it.book.id} state=${it.state}")
                it.book.id == id && it.state != State.RETURNED }

        if (activeReservations.isNotEmpty()) {
            throw ConflictException("No se puede eliminar un libro con reservas activas")
        }

        bookRepository.delete(id)
    }

    /*
    fun updateBook(updatedBook : BookDTO) : Book {
        val newBook = updatedBook.fromDTO()
    }*/

    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable ): PageResponse<BookDTO> {
        val reservedBookIds : Set<Int> = reservationRepository.findReservedBookIds(searchCriteria)
        val page : Page<Book> = bookRepository.findAllByCriteria(searchCriteria, reservedBookIds, pageable)

        val booksWithBibliokarmasDTO : List<BookDTO> = getBooksBibliokarmasDTO(page.content, searchCriteria)

        return PageResponse(
            content = booksWithBibliokarmasDTO,
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

    fun getUser(id: Int): User {
        return userRepository.getObject(id)
    }

    fun getBookById(id: Int): Book =
        bookRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")}

