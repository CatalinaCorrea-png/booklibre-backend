package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.*
import ar.edu.unsam.phm.specification.BookSpecifications
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BookService(
    @Autowired
    val bookRepository: MongoBookRepository,
    @Autowired
    val reservationRepository: CrudReservationRepository,
    @Autowired
    val mongoReservationRepository: MongoReservationRepository,
    @Autowired
    val userRepository: CrudUserRepository,
    @Autowired
    val authorRepository: CrudAuthorRepository,
    @Autowired
    val reviewRepository: CrudReviewRepository,
) {
    @Transactional
    fun createBook(bookCreateDTO: BookCreateDTO) {
        val owner = userRepository.findById(bookCreateDTO.ownerId)
            .orElseThrow { NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}") }

        val author: Author = authorRepository.findByName(bookCreateDTO.book.author.name)
            .orElseGet {
                authorRepository.save(
                    Author(
                        name = bookCreateDTO.book.author.name
                    )
                )
            }

        val newBook = bookCreateDTO.createFromDTO(owner)
        newBook.author = author
        //println("author name del DTO: ${bookCreateDTO.book.author.name}")
        newBook.validate()
        bookRepository.save(newBook)
    }

    @Transactional
    fun updateBook(id: String, bookCreateDTO: BookCreateDTO) : Book {
        val bookOwner = userRepository.findById(bookCreateDTO.ownerId)
            .orElseThrow { NotFoundException("No existe el usuario con id: ${bookCreateDTO.ownerId}") }

        val existingBook = bookRepository.findById(id)
            .orElseThrow { NotFoundException("No existe el libro con id: $id") }

        if (existingBook.deleted) {
            throw ConflictException("No se puede modificar un libro eliminado")
        }

        if (bookOwner.id != existingBook.owner.id) {
            throw ConflictException("No podes modificar un libro que no es tuyo.")
        }


        val author: Author = authorRepository.findByName(bookCreateDTO.book.author.name)
            .orElseGet {
                authorRepository.save(
                    Author(
                        name = bookCreateDTO.book.author.name,
                        avatar = bookCreateDTO.book.author.avatar
                    )
                )
            }

        //val newBook = bookCreateDTO.createFromDTO(bookOwner)
        existingBook.apply {
            title = bookCreateDTO.book.title
            desc = bookCreateDTO.book.desc
            gender = bookCreateDTO.book.gender
            numPages = bookCreateDTO.book.numPages
            isbn = bookCreateDTO.book.isbn
            language = bookCreateDTO.book.language
            editorial = bookCreateDTO.book.editorial
            publishDate = bookCreateDTO.book.publishDate
            condition = bookCreateDTO.book.condition
            imageSrc = bookCreateDTO.book.imageSrc
            this.author = author
            this.owner = bookOwner.toOwnerDTO()
        }
        existingBook.validate()
        return bookRepository.save(existingBook)
    }

    @Transactional
    fun deleteBook(bookId: String) {
        val book = bookRepository.findById(bookId)
            .orElseThrow { NotFoundException("No existe el libro con id: $bookId") }

        val today = LocalDate.now()
        val isBorrowed = book.reservations.any {
            it.pickUpDate <= today && it.dropOffDate >= today
        }

        if (isBorrowed) throw ConflictException("No se puede eliminar un libro que está prestado")

        book.logicDelete()
        bookRepository.save(book)
        reservationRepository.markBookAsDeletedInReservations(book.bookId)
    }

    fun getAllUserBooks(
        userId: String,
        pageableObject: ProfileBookPageable
    ): PagedResult<ProfileBookDTO> {
        val today = LocalDate.now()

        val booksPage = bookRepository.findUserBooks(
            userId,
            pageableObject.filterCriteria.bookFilter(today),
            pageableObject.toPageRequest()
        )

        return PagedResult(
            items = booksPage.content.map { it.toProfileBookDTO(today) },
            total = booksPage.totalElements.toInt(),
            totalPages = booksPage.totalPages
        )
    }


    @Transactional(readOnly = true)
    fun searchBooks(searchCriteria: BookSearchCriteria, pageable: Pageable): PageResponse<BookDTO> {
        // println("Criteria: $searchCriteria")

        // Query paginada con Criteria de Mongo
        val criteria = BookSpecifications.byCriteriaMongo(searchCriteria)
        val page : Page<Book> = bookRepository.findByCriteria(criteria, pageable)
        // println("Results: ${page.totalElements}")

        val booksWithBibliokarmasDTO : List<BookDTO> = getBooksBibliokarmasDTO(page.content, searchCriteria)
        return PageResponse(
            content = booksWithBibliokarmasDTO,
            page = page.number,
            pageSize = page.size,
            totalElements = page.totalElements.toInt(),
            totalPages = page.totalPages
        )
    }

    private fun getBooksBibliokarmasDTO(books: List<Book>, criteria: BookSearchCriteria): List<BookDTO> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)
        val user = userRepository.findById(criteria.userId!!)
            .orElseThrow { NotFoundException("No existe user con id: ${criteria.userId}") }
        val bookDTOs = books.map { book ->
            val bookDTO = book.toDTO()
            bookDTO.bookBibliokarmas = book.calculateBibliokarmas(reservationTemp.reservationDays(), user.bibliokarmas)
            bookDTO
        }
        return bookDTOs
    }

    // la sesión vive hasta que termina el metodo
    @Transactional(readOnly = true)
    fun getBookById(id: String): BookDTO = bookRepository
        .findById(id)
        .orElseThrow {
            NotFoundException("No se encuentra un libro registrado con el id: $id!!!!")
        }
        .toDTO()

    //trae todos los libros menos los que fueron eliminados logicamente IMPORTANTE USAR ESTE METODO SINO VA A TRAER LIBROS QUE FUERON BORRADOS LOGICAMENTEEEE
//    @Transactional(readOnly = true)
//    fun getAllBooks(): List<Book> = bookRepository.findAllByDeletedIsFalse()

    @Transactional(readOnly = true)
    fun recalculateBibliokarmas(bookId: String, userId: String, pickUpDate: LocalDate, dropOffDate: LocalDate): Long {
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

    @Transactional(readOnly = true)
    fun getBookReviews(bookId: String, page: Int = 0, pageSize: Int = 2): List<Review> {
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "timestamp"))
        val book = bookRepository.findById(bookId)
            .orElseThrow { NotFoundException("No se encuentra un libro registrado con el id: $bookId") }
        return reviewRepository.findReviewsByBookId(book.bookId, pageable).content
    }
}