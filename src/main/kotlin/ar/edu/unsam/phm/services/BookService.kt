package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BookService(
    @Autowired
    val bookRepository: CrudBookRepository,
    @Autowired
    val reservationRepository: CrudReservationRepository,
    @Autowired
    val userRepository: CrudUserRepository,
    @Autowired
    val authorRepository: CrudAuthorRepository
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

        if (reservations.any { it.state == State.BORROWED || State.RESERVED == it.state || State.ACTIVE == it.state || State.SOON_TO_END == it.state}) {
            throw ConflictException("No se puede eliminar un libro que está prestado")
        }

        reservations
            .filter { it.state == State.RESERVED }
            .forEach { reservationRepository.delete(it) }

        book.logicDelete()
        bookRepository.save(book)  // guarda el libro con el delete logico, no lo borra de la coleccion
    }

    @Transactional(readOnly = true)
    fun getAllUserBooks(
        userId: Long,
        pageableObject: ProfileBookPageable
    ): PagedResult<ProfileBookDTO> {
        val pageable: PageRequest = pageableObject.toPageRequest()
        println(pageable)
        val booksPage: Page<ProfileBookDTO> = bookRepository.getAllUserBooks(userId, pageable, pageableObject.filterCriteria.name)
        println(booksPage)
        val booksPageContent = booksPage.content // No se que clase es esto (Mutable)List<ProfileBookDTO!>
        return PagedResult(booksPageContent, booksPage.size, booksPage.totalPages)
    }

    @Transactional(readOnly = true)
    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable ): PageResponse<BookDTO> {
//        println("Criteria: $searchCriteria")
//        println("Pageable: $pageable")
        val page : Page<Book> = bookRepository.findAllByCriteria(
            userId = searchCriteria.userId,
            title = searchCriteria.title,
            genders = searchCriteria.genders,
            pagesRangeMin = searchCriteria.pagesRangeMin,
            pagesRangeMax = searchCriteria.pagesRangeMax,
            pickUpDate = searchCriteria.pickUpDate,
            dropOffDate = searchCriteria.dropOffDate,
            isbn = searchCriteria.isbn,
            ownersName = searchCriteria.ownersName,
            pageable = pageable
        )
//        println("Results: ${page.totalElements}")
        val booksWithBibliokarmasDTO : List<BookDTO> = getBooksBibliokarmasDTO(page.content, searchCriteria)
        return PageResponse(
            content = booksWithBibliokarmasDTO,
            page = page.number,
            pageSize = page.size,
            totalElements = page.totalElements.toInt(),
            totalPages = page.totalPages
        )
    }

    private fun getBooksBibliokarmasDTO(books: List<Book>, criteria: BookSearchCriteria) : List<BookDTO> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)
        val user = userRepository.findById(criteria.userId!!).orElseThrow{ NotFoundException("No existe user con id: ${criteria.userId}") }
        val bookDTOs = books.map { book ->
            val bookDTO = book.toDTO()
            bookDTO.bookBibliokarmas = book.calculateBibliokarmas(reservationTemp.reservationDays(), user.bibliokarmas)
            bookDTO
        }
        return bookDTOs
    }

    // la sesión vive hasta que termina el metodo
    @Transactional(readOnly = true)
    fun getBookById(id: Long): BookDTO = bookRepository
        .findById(id)
        .orElseThrow {
            NotFoundException("No se encuentra un libro registrado con el id: $id")
        }
        .toDTO()

    //trae todos los libros menos los que fueron eliminados logicamente IMPORTANTE USAR ESTE METODO SINO VA A TRAER LIBROS QUE FUERON BORRADOS LOGICAMENTEEEE
    @Transactional(readOnly = true)
    fun getAllBooks(): List<Book> = bookRepository.findAllByDeletedIsFalse()

    @Transactional(readOnly = true)
    fun recalculateBibliokarmas(bookId: Long, userId: Long, pickUpDate: LocalDate, dropOffDate: LocalDate): Int {
        val book = bookRepository.findById(bookId)
            .orElseThrow {
                NotFoundException("No se encuentra un libro registrado con el id: $bookId")
            }
        val user = userRepository.findById(userId)
            .orElseThrow {
                NotFoundException("No se encuentra un usuario registrado con el id: $userId")
            }
        val days = Reservation(pickUpDate = pickUpDate, dropOffDate = dropOffDate).reservationDays()
        return book.calculateBibliokarmas(days, user.bibliokarmas)
    }
}