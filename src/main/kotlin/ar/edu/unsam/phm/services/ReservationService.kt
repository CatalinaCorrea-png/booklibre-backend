package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.State
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.PagedResult
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.dto.ReservedPeriodDTO
import ar.edu.unsam.phm.dto.toReservationProfileDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.Repository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.ceil

@Service
class ReservationService(
    val reservationRepository: ReservationRepository,
    val bookRepository: BookRepository,
    val userRepository: UserRepository
){
    fun createReservation(reservation: Reservation) {
        reservation.validate()
        if (!canReserve(reservation)) throw BusinessException("Reserva no disponible en esa fecha")
        reservationRepository.create(reservation)

        // Acá sumo la reserva al libro?????
        val book = bookRepository.getObject(reservation.book.id)
        book.addReservation(reservation.id)
//        reservation.state = State.BORROWED

        val user = userRepository.getObject(reservation.user.id)
        user.addBibliokarmas(book.calculateBibliokarmas(reservation))
    }

    // esto tiene que estar negado asi devuelve true si no hay solapamiento
    fun canReserve(reservation: Reservation) : Boolean = reservationRepository.repositoryObjects().none { it.book.id == reservation.book.id && it.dateOverlaps(reservation) }

    fun getAvailableReservations(reservation: Reservation) : MutableList<Reservation> {
        return reservationRepository.repositoryObjects().filter { it.dateOverlaps(reservation) }.toMutableList()
    }

    fun getReservesByUserId(userId: Int, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
        val filtered = reservationRepository.findByLectorId(userId).filter { res ->
            res.book.title.contains(search, ignoreCase = true) ||
                    res.book.author.name.contains(search, ignoreCase = true)
        }.map { it.toDTO()}
        return PagedResult(
            items = filtered.drop(page * pageSize).take(pageSize),
            total = filtered.size,
            totalPages = Math.ceil(filtered.size.toDouble() / pageSize).toInt()
        )
    }

    fun getLoansMadeByUserId(userId: Int, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
        val filtered = reservationRepository.findByOwnerId(userId).filter { res ->
            res.book.title.contains(search, ignoreCase = true) ||
                    res.book.author.name.contains(search, ignoreCase = true)
        }.map { it.toDTO()}

        return PagedResult(
            items = filtered.drop(page * pageSize).take(pageSize),
            total = filtered.size,
            totalPages = Math.ceil(filtered.size.toDouble() / pageSize).toInt()
        )
    }

    fun rateLoan(reservationId: Int, puntuacion: Int, comentario: String, userId: Int) {
        val reservation = reservationRepository.getObject(reservationId)

        // le pongo la review desde aca, no se si esta bien
        reservation.review.apply {
            this.rating = puntuacion
            this.review = comentario
            this.reviewerName = userRepository.getObject(userId).name
        }

        //! acordate de actualizarlo bobo
        reservationRepository.update(reservation)
    }

    fun getUserReservationsNumber(userId: Int): Int =
        reservationRepository.repositoryObjects().filter { reservation ->
            reservation.holderId() == userId && reservation.dropOffDate.isBefore(LocalDate.now()) }.size

    fun getUserLentBooksNumber(userId: Int): Int {
        val userReserves: List<Reservation> = reservationRepository.repositoryObjects().filter { reservation ->
            reservation.bookOwnerId() == userId }

        val userReservesDTO: List<ReservationProfileDTO> = userReserves.map { it.toReservationProfileDTO() }

        return userReservesDTO.filter { reservation -> reservation.state == "Prestado" }.size

    }


    private fun generateEmptyReservationsForNotReservedBooks(booksList: List<Book>): List<Reservation> =
        booksList.map { book -> Reservation(book = book, pickUpDate = LocalDate.of(1000, 1, 1), dropOffDate = LocalDate.of(1000, 2, 1)) }

    private fun getEveryUserBook(userId: Int): List<Book> =
        bookRepository.repositoryObjects().filter { book -> book.owner.id == userId }

    private fun getEveryReservationWithUserBook(userId: Int): List<Reservation> =
        reservationRepository.repositoryObjects().filter { reserve -> reserve.bookOwnerId() == userId }

    private fun filterNoReservedBooks(books: List<Book>, reservations: List<Reservation>): List<Book> =
        books.filter { book -> reservations.none { reservation -> reservation.book.id == book.id} }


    fun getUserOwnBooks(userId: Int): List<Reservation> {

        val everyUserOwnBook: List<Book> = this.getEveryUserBook(userId)

        val reservationsWithBooksOwnByUser: List<Reservation> = this.getEveryReservationWithUserBook(userId)

        val userNotReservedBooks = this.filterNoReservedBooks(everyUserOwnBook, reservationsWithBooksOwnByUser)

        val userNotReservedBooksInReservation = generateEmptyReservationsForNotReservedBooks(userNotReservedBooks)

        val everyUserBookInReservation = (reservationsWithBooksOwnByUser + userNotReservedBooksInReservation).distinctBy { it.book.id }

        return everyUserBookInReservation
    }

    fun filterAndSortUserBooks(bookList: List<ReservationProfileDTO>, page: Int, pageSize: Int, filterCrit: FilterCriteria, sortCrit: SortCriteria): PagedResult<ReservationProfileDTO> {
        var filteredAndSortedBookList: List<ReservationProfileDTO> = bookList
                                                                        .filter ( filterCrit.predicate ) // equiv. to { reservation -> filterCrit.predicate(reservation) }
                                                                        .sortedWith ( sortCrit.comparator )
        return PagedResult(
            items = filteredAndSortedBookList.drop(page * pageSize).take(pageSize),
            total = filteredAndSortedBookList.size,
            totalPages = ceil(filteredAndSortedBookList.size.toDouble() / pageSize).toInt()
        )
    }

    fun getBookReviews(bookId: Int, page: Int = 0, pageSize: Int = 2): List<Review> {
        return reservationRepository.repositoryObjects()
            .filter { it.book.id == bookId && it.review.notEmptyReview() }
            .sortedByDescending { it.review.timestamp }
            .drop(page * pageSize)
            .take(pageSize)
            .map { it.review }
    }

    fun getReservedDates(bookId: Int): List<ReservedPeriodDTO> =
        reservationRepository.repositoryObjects()
            .filter { it.book.id == bookId }
            .map { ReservedPeriodDTO(it.pickUpDate, it.dropOffDate) }

//    fun getBookAverageRating(bookId: Int): Double {
//        val reviews = reservationRepository.repositoryObjects()
//            .filter { it.book.id == bookId }
//            .filter { it.review.rating > 0 }
//            .map { it.review.rating }
//
//        return if (reviews.isEmpty()) 0.0 else reviews.average()
//    }

}