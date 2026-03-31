package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

//@org.springframework.stereotype.Repository
@Component
class BookRepository(): Repository<Book>() {

    fun findAllByUserId(userId: Long): List<Book> =
        this.repositoryObjects().filter { book -> book.owner.id == userId }

    fun findAllByCriteria(criteria: BookSearchCriteria): List<Book> {

        val filteredAndAvailable = this.repositoryObjects().filter { book ->
            book.owner.id != criteria.userId &&     // no me traigo mis propios libros
                    // filtros de busqueda de libro
                    book.meetsSearchCriteria(criteria.title?.trim()!!) &&
                    (criteria.genders.isEmpty() ||
                            criteria.genders.any { gender -> book.matchesPartiallyWith(gender.value, book.gender.value) } )&&
                    book.meetsPagesCriteria(criteria.pagesRangeMin, criteria.pagesRangeMax) &&
                    book.matchesPartiallyWith(criteria.isbn?.trim()!!, book.isbn) &&
                    book.matchesPartiallyWith(criteria.ownersName?.trim()!!, book.owner.name)
        }

        return filteredAndAvailable
    }

    fun sortAndPage(books: List<Book>, pageable: Pageable) : Page<Book> {
        val sorted = sortInMemory(books, pageable.sort)

        val from = (pageable.pageNumber * pageable.pageSize).coerceAtMost(sorted.size) // primer libro de la pagina
        val to = (from + pageable.pageSize).coerceAtMost(sorted.size) // ultimo libro de la pagina

        return PageImpl(
            /*content= */ sorted.subList(from, to), // el contenido de esta página
            /*pageable= */ pageable,                // el criterio con el que se pidió
            /*total= */ sorted.size.toLong())       // el TOTAL de elementos (no solo los de esta página)
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

    fun getObjectsByIds(bookIds: List<Long>): List<Book> =
        this.repositoryObjects().filter { book -> bookIds.any { id -> book.id == id } }

}