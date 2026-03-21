package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
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
        println("llamando meetsCreationCriteria")
        book.meetsCreationCriteria()
        println("pasó meetsCreationCriteria")
        bookRepository.create(book)
    }

    fun updateBook(id: Int, book: Book) {
        val existingBook = bookRepository.getObject(id)
        book.id = existingBook.id
        book.meetsCreationCriteria()
        bookRepository.update(book)
    }

    /*
    fun updateBook(updatedBook : BookDTO) : Book {
        val newBook = updatedBook.fromDTO()
    }*/

    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable ): PageResponse<BookDTO> {
        val reservedBookIds = reservationRepository.findReservedBookIds(searchCriteria)
        val filteredAndAvailable = bookRepository.findAllByCriteria(searchCriteria, reservedBookIds)

        val ordered = sortInMemory(filteredAndAvailable, pageable.sort)
        val paged = paginate(ordered,pageable)

        paged.content = getBooksBibliokarmas(paged.content, searchCriteria)
        return paged
    }

    // Ahora lo hago aca, luego se hace en Repo con Query ?
    private fun sortInMemory(books: List<Book>, sort: Sort): List<Book> {
        val order = sort.firstOrNull() ?: return books
        val field = BookSortField.from(order.property) // Creo/Elijo la criteria para el sorting

        return if (order.isAscending)
            books.sortedBy { field.selector(it) } // selector es "title", "owner" o "author"
        else
            books.sortedByDescending { field.selector(it) }
    }

    // Ahora lo hago aca, luego se hace en Repo con Query ?
    private fun paginate(books: List<Book>, pageable: Pageable): PageResponse<BookDTO> {
        // Cuantas paginas son
        val total = books.size
        val totalPages = if (total == 0) 0 else ceil(total.toDouble() / pageable.pageSize).toInt()
        // Qué pagina devuelvo
        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(total) // primer libro de la pagina
        val to = (from + pageable.pageSize).coerceAtMost(total) // ultimo libro de la pagina
        val paged = books.subList(from, to)

        return PageResponse(
            content = paged.map { it.toDTO() },
            page = pageable.pageNumber,
            pageSize = pageable.pageSize,
            totalElements = total,
            totalPages = totalPages
        )
    }

    fun getBooksBibliokarmas(bookDTOs: List<BookDTO>, criteria: BookSearchCriteria) : List<BookDTO> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)
        val user = userRepository.getObject(criteria.userId)
        bookDTOs.forEach { bookDTO ->
            bookDTO.bookBibliokarmas = bookRepository.getObject(bookDTO.id).calculateBibliokarmas(reservationTemp.reservationDays(), user.bibliokarmas)
        }
        return bookDTOs
    }

    fun getUser(id: Int): User {
        println("usuarios en repo: ${userRepository.repositoryObjects().size}")
        println("buscando usuario con id: $id")
        return userRepository.getObject(id)
    }

    fun getBookById(id: Int): Book =
        bookRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")

}