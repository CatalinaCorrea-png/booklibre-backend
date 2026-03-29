package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import java.time.LocalDate
import kotlin.math.ceil

@org.springframework.stereotype.Repository
class ReservationRepository: Repository<Reservation>() {

    fun findByLectorId(userId: Int): List<Reservation> =
        repositoryObjects().filter { it.user.id == userId }

    fun findByOwnerId(userId: Int): List<Reservation> =
        repositoryObjects().filter { it.book.owner.id == userId }

    fun findByBookId(bookId: Int): List<Reservation> {
        return this.repositoryObjects().filter { it.book.id == bookId }
    }

    fun findRatingsByBookId(bookId: Int): List<Int> {
        return this.repositoryObjects().filter { it.book.id == bookId }.map { it.review.rating }
    }

    fun findReservedBookIds(criteria: BookSearchCriteria): Set<Int> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)

        return this.repositoryObjects()
            .filter { it.dateOverlaps(reservationTemp) }
            .map { it.book.id }
            .toSet()
    }

    fun updateBookReference(updatedBook: Book) {
        collection
            .filter { it.book.id == updatedBook.id }
            .forEach { it.book = updatedBook }
    }

    fun deleteAllReservationsByBookId(bookId: Int) =
        findByBookId(bookId)
            .forEach { delete(it.id) }

    fun filterNoReservedBooks(books: List<Book>): List<Book> =
        books.filter { book -> this.repositoryObjects().none { reservation -> reservation.book.id == book.id } }

    fun getFilteredAndSortedReservations(userId: Int, fictitiouslyGenReservations: List<Reservation>, page: Int, pageSize: Int, filterCrit: FilterCriteria, sortCrit: SortCriteria): PagedResult<ReservationProfileDTO> {
        val userReservations: List<Reservation> = this.findByOwnerId(userId)
        val distAndSort: List<Reservation> = this.sortByDescAndDistinct(userReservations + fictitiouslyGenReservations)
        return this.filterAndSortReservations(distAndSort, page, pageSize, filterCrit, sortCrit)

    }

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

    fun hasReservationsForBook(bookId: Int): Boolean =
        repositoryObjects().any { it.book.id == bookId }

    fun hasDateOverlap(reservation: Reservation): Boolean =
        repositoryObjects().none { it.dateOverlaps(reservation) }

    fun hasOverlappingReservation(bookId: Int, reservation: Reservation): Boolean =
        repositoryObjects().none { hasReservationsForBook(bookId) && hasDateOverlap(reservation) }

    fun findReviewsByBookId(bookId: Int): List<Reservation> =
        findByBookId(bookId)
            .filter { it.review.notEmptyReview() }
            .sortedByDescending { it.review.timestamp }
}