package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class BookService(
    val bookRepository: BookRepository,
    val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
) {
    fun createBook(book: Book){
        println("llamando meetsCreationCriteria")
        println("imageSrc recibido: '${book.imageSrc}'")
        println("timestamp recibido: '${book.timestamp}'")
        book.meetsCreationCriteria()
        println("pasó meetsCreationCriteria")
        bookRepository.create(book)
    }

    fun updateBook(id: Int, book: Book) {
        val existingBook = bookRepository.getObject(id)
        println("imageSrc recibido: '${book.imageSrc}'")
        println("imageSrc recibido: '${book.imageSrc}'")
        println("timestamp recibido: '${book.timestamp}'")
        book.id = existingBook.id
        book.meetsCreationCriteria()
        bookRepository.update(book)
    }

    /*
    fun updateBook(updatedBook : BookDTO) : Book {
        val newBook = updatedBook.fromDTO()
    }*/

    fun getAvailableBooksBy(criteria: BookSearchCriteria): PageResponse<BookDTO> {
        val allBooks = bookRepository.repositoryObjects()

        val filtered = allBooks.filter { book ->
            matchesTitle(book, criteria) &&
                    matchesGender(book, criteria) &&
                    matchesPages(book, criteria) &&
                    matchesISBN(book, criteria) &&
                    matchesOwner(book, criteria) &&
                    matchesAvailability(book, criteria)
        }

        val ordered = criteria.sortedBy.sort(filtered, criteria.ascending)

        // PAGINACION
        // Cuantas paginas son
        val totalElements = ordered.size
        val totalPages = if (totalElements == 0) 0 else Math.ceil(totalElements.toDouble() / criteria.pageSize).toInt()
        // Qué pagina devuelvo
        val fromIndex = (criteria.page * criteria.pageSize).coerceAtMost(totalElements) // primer libro de la pagina
        val toIndex = (fromIndex + criteria.pageSize).coerceAtMost(totalElements) // ultimo libro de la pagina
        // Creo la lista de libros por pagina
        val paged = ordered.subList(fromIndex, toIndex)



        return PageResponse(
            content = paged.map { it.toDTO() },
            page = criteria.page,
            pageSize = criteria.pageSize,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }


    fun getUser(id: Int): User {
        println("usuarios en repo: ${userRepository.repositoryObjects().size}")
        println("buscando usuario con id: $id")
        return userRepository.getObject(id)
    }

    // FILTROS DE BUSQUEDA >.<
    private fun matchesTitle(book: Book, criteria: BookSearchCriteria): Boolean {
        val title = criteria.title?.trim()
        return title.isNullOrBlank() || book.title.contains(title, ignoreCase = true)
    }

    private fun matchesGender(book: Book, criteria: BookSearchCriteria): Boolean {
        return criteria.genders.isEmpty() || criteria.genders.contains(book.gender)
    }

    private fun matchesPages(book: Book, criteria: BookSearchCriteria): Boolean {
        val min = criteria.pagesRangeMin ?: 0
        val max = criteria.pagesRangeMax ?: 1500 // Regla de negocio (Por ahora)
        return book.numPages in min..max
    }

    private fun matchesISBN(book: Book, criteria: BookSearchCriteria): Boolean {
        val isbn = criteria.isbn?.trim()
        return isbn.isNullOrBlank() || book.isbn.contains(isbn, ignoreCase = true)
    }

    private fun matchesOwner(book: Book, criteria: BookSearchCriteria): Boolean {
        val ownerName = criteria.ownersName?.trim()
        return ownerName.isNullOrBlank() || book.owner.name.contains(ownerName, ignoreCase = true)
    }

    private fun matchesAvailability(book: Book, criteria: BookSearchCriteria): Boolean {
        val pickUp = criteria.pickUpDate
        val dropOff = criteria.dropOffDate
        val reservationTemp = Reservation(pickUpDate = pickUp, dropOffDate = dropOff)

        // No deberian ser null, pero por las dudas?
        if (pickUp == null || dropOff == null) return true

        // Me traigo todas las reservas del libro a chequear
        val reservations = reservationRepository.repositoryObjects().filter { it.book.id == book.id }

        // Si al menos una reserva coincide en fecha, no está disponible.
        return reservations.none { it.dateOverlaps(reservationTemp) }
    }

    fun getBookById(id: Int): Book =
        bookRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")}

