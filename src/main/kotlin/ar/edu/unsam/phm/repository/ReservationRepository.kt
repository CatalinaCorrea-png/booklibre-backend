package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.ceil

@Component
class ReservationRepository: Repository<Reservation>() {

    fun findByLectorId(userId: Long): List<Reservation> =
        repositoryObjects().filter { it.user.id == userId }

    fun findByOwnerId(userId: Long): List<Reservation> =
        repositoryObjects().filter { it.book.owner.id == userId }

    fun findByLectorIdFiltered(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<Reservation> {
        val filtered = findByLectorId(userId)
            .filter { res ->
                res.book.title.contains(search, ignoreCase = true) ||
                        res.book.author.name.contains(search, ignoreCase = true)
            }

        return PagedResult(
            items = filtered.drop(page * pageSize).take(pageSize),
            total = filtered.size,
            totalPages = ceil(filtered.size.toDouble() / pageSize).toInt()
        )
    }

    fun findByOwnerIdFiltered(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<Reservation> {
        val filtered = findByOwnerId(userId)
            .filter { res ->
                res.book.title.contains(search, ignoreCase = true) ||
                        res.book.author.name.contains(search, ignoreCase = true)
            }

        return PagedResult(
            items = filtered.drop(page * pageSize).take(pageSize),
            total = filtered.size,
            totalPages = ceil(filtered.size.toDouble() / pageSize).toInt()
        )
    }

    fun findByBookId(bookId: Long): List<Reservation> {
        return this.repositoryObjects().filter { it.book.id == bookId }
    }

    fun findRatingsByBookId(bookId: Long): List<Int> {
        return this.repositoryObjects().filter { it.book.id == bookId }.map { it.review.rating }
    }

    fun findReservedBookIds(criteria: BookSearchCriteria): Set<Long> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)

        return this.repositoryObjects()
            .filter { it.dateOverlaps(reservationTemp) }
            .map { it.book.id!! }
            .toSet()
    }

    fun updateBookReference(updatedBook: Book) {
        collection
            .filter { it.book.id == updatedBook.id }
            .forEach { it.book = updatedBook }
    }

    fun deleteAllReservationsByBookId(bookId: Long) =
        findByBookId(bookId)
            .forEach { delete(it.id!!) }

    fun filterNoReservedBooks(books: List<Book>): List<Book> =
        books.filter { book -> this.repositoryObjects().none { reservation -> reservation.book.id == book.id } }

    fun getFilteredAndSortedReservations(userId: Long, fictitiouslyGenReservations: List<Reservation>, page: Int, pageSize: Int, filterCrit: FilterCriteria, sortCrit: SortCriteria): PagedResult<ReservationProfileDTO> {
        val userReservations: List<Reservation> = this.findByOwnerId(userId)
        val distAndSort: List<Reservation> = this.sortByDescAndDistinct(userReservations + fictitiouslyGenReservations)
        return this.filterAndSortReservations(distAndSort, page, pageSize, filterCrit, sortCrit)

    }
    fun getUserReservationsNumber(userId: Long): Int = this.repositoryObjects().filter { reservation ->
        reservation.holderId() == userId && reservation.dropOffDate.isBefore(LocalDate.now()) }.size

    fun getUserLentBooksNumber(userId: Long): Int = this.repositoryObjects().filter { reservation ->
        reservation.bookOwnerId() == userId && (reservation.state == State.ACTIVE || reservation.state == State.BORROWED || reservation.state == State.SOON_TO_END)}.size

    fun sortByDescAndDistinct(reservations: List<Reservation>) =
        reservations.sortedByDescending { it.pickUpDate }.distinctBy { it.book.id }

    fun filterAndSortReservations(reservations: List<Reservation>, page: Int, pageSize: Int, filterCrit: FilterCriteria, sortCrit: SortCriteria): PagedResult<ReservationProfileDTO> {
        val filteredAndSortedBookList: List<ReservationProfileDTO> = reservations
            .filter ( filterCrit.predicate ) // equiv. to { reservation -> filterCrit.predicate(reservation) }
            .sortedWith ( sortCrit.comparator )
            .map { it.toReservationProfileDTO() }

        return PagedResult(
            items = filteredAndSortedBookList.drop(page * pageSize).take(pageSize),
            total = filteredAndSortedBookList.size,
            totalPages = ceil(filteredAndSortedBookList.size.toDouble() / pageSize).toInt()
        )
    }

    fun hasReservationsForBook(bookId: Long): Boolean =
        repositoryObjects().any { it.book.id == bookId }

    fun hasDateOverlap(reservation: Reservation): Boolean =
        repositoryObjects().none { it.dateOverlaps(reservation) }

    fun hasOverlappingReservation(bookId: Long, reservation: Reservation): Boolean =
        repositoryObjects().none { hasReservationsForBook(bookId) && hasDateOverlap(reservation) }

    fun findReviewsByBookId(bookId: Long): List<Reservation> =
        findByBookId(bookId)
            .filter { it.review.notEmptyReview() }
            .sortedByDescending { it.review.timestamp }
}