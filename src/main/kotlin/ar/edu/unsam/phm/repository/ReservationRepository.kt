package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Reservation
import java.time.LocalDate

@org.springframework.stereotype.Repository
class ReservationRepository: Repository<Reservation>() {

    fun findByLectorId(userId: Int): List<Reservation> =
        repositoryObjects().filter { it.user.id == userId }

    fun findByOwnerId(userId: Int): List<Reservation> =
        repositoryObjects().filter { it.book.owner.id == userId }

    fun findByBookId(bookId: Int): List<Reservation> {
        return this.repositoryObjects().filter { it.book.id == bookId }
    }

    fun findReservedBookIds(criteria: BookSearchCriteria): Set<Int> {
        val reservationTemp = Reservation(pickUpDate = criteria.pickUpDate, dropOffDate = criteria.dropOffDate)

        return this.repositoryObjects()
            .filter { it.dateOverlaps(reservationTemp) }
            .map { it.book.id }
            .toSet()
    }
}