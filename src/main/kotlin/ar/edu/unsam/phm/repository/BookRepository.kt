package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.toDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.math.ceil

@org.springframework.stereotype.Repository
class BookRepository(
    private val reservationRepository: ReservationRepository  // inyección por constructor
): Repository<Book>() {

    fun findAllByCriteria(criteria: BookSearchCriteria, reservedBookIds: Set<Int>, pageable: Pageable): Page<Book> {
        val allBooks = this.repositoryObjects()

        val filteredAndAvailable = this.repositoryObjects().filter { book ->
            book.owner.id != criteria.userId &&     // no me traigo mis propios libros
            book.id !in reservedBookIds &&          // no está reservado
            matchesTitle(book, criteria) &&         // filtros de busqueda de libro
            matchesGender(book, criteria) &&
            matchesPages(book, criteria) &&
            matchesISBN(book, criteria) &&
            matchesOwner(book, criteria)
        }

        val sorted = sortInMemory(filteredAndAvailable, pageable.sort)

        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sorted.size) // primer libro de la pagina
        val to = (from + pageable.pageSize).coerceAtMost(sorted.size) // ultimo libro de la pagina

        return PageImpl(sorted.subList(from, to), pageable, sorted.size.toLong())
    }

    // Ahora lo hago aca, luego se hace con Query ?
    private fun sortInMemory(books: List<Book>, sort: Sort): List<Book> {
        val order = sort.firstOrNull() ?: return books
        val field = BookSortField.from(order.property) // Creo/Elijo la criteria para el sorting

        return if (order.isAscending)
            books.sortedBy { field.selector(it) } // selector es "title", "owner" o "author"
        else
            books.sortedByDescending { field.selector(it) }
    }

//    private fun paginate(books: List<Book>, pageable: Pageable): PageResponse<BookDTO> {
//        // Cuantas paginas son
//        val total = books.size
//        val totalPages = if (total == 0) 0 else ceil(total.toDouble() / pageable.pageSize).toInt()
//        // Qué pagina devuelvo
//        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(total) // primer libro de la pagina
//        val to = (from + pageable.pageSize).coerceAtMost(total) // ultimo libro de la pagina
//        val paged = books.subList(from, to)
//
//        return PageResponse(
//            content = paged.map { it.toDTO() },
//            page = pageable.pageNumber,
//            pageSize = pageable.pageSize,
//            totalElements = total,
//            totalPages = totalPages
//        )
//    }

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

//    SORTING
//    fun getOrderedBooks(fileredBooks: List<Book>, sortingCriteria: SortingCriteria): List<Book> {
//        return sortingCriteria.sortedBy.sort(fileredBooks, sortingCriteria.ascending)
//    }

//    PAGINACION
//    fun getPagedBooks(orderedBooks: List<Book>, pagingCriteria: PageRequest): PageResponse<BookDTO> {
//        // Cuantas paginas son
//        val totalElements = orderedBooks.size
//        val totalPages = if (totalElements == 0) 0 else Math.ceil(totalElements.toDouble() / pagingCriteria.pageSize).toInt()
//        // Qué pagina devuelvo
//        val fromIndex = (pagingCriteria.page * pagingCriteria.pageSize).coerceAtMost(totalElements) // primer libro de la pagina
//        val toIndex = (fromIndex + pagingCriteria.pageSize).coerceAtMost(totalElements) // ultimo libro de la pagina
//        // Creo la lista de libros por pagina
//        val paged = orderedBooks.subList(fromIndex, toIndex)
//
//
//
//        return PageResponse(
//            content = paged.map { it.toDTO() },
//            page = pagingCriteria.page,
//            pageSize = pagingCriteria.pageSize,
//            totalElements = totalElements,
//            totalPages = totalPages
//        )
//    }

}