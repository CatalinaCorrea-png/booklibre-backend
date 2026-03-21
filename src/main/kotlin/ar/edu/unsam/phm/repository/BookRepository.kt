package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.BookDTO
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.toDTO

@org.springframework.stereotype.Repository
class BookRepository(
    private val reservationRepository: ReservationRepository  // inyección por constructor
): Repository<Book>() {

    fun findAllByCriteria(criteria: BookSearchCriteria, reservedBookIds: Set<Int>): List<Book> {
        val allBooks = this.repositoryObjects()

        val filtered = allBooks.filter { book ->
            matchesTitle(book, criteria) &&
                    matchesGender(book, criteria) &&
                    matchesPages(book, criteria) &&
                    matchesISBN(book, criteria) &&
                    matchesOwner(book, criteria)
        }

        val filteredAndAvailable = filtered.filter { it.id !in reservedBookIds }

        return filteredAndAvailable
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