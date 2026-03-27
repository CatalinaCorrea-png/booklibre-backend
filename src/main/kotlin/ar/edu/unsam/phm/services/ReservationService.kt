package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.dto.PagedResult
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.dto.ReservedPeriodDTO
import ar.edu.unsam.phm.dto.toReservationProfileDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
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
    fun createReservation(reservation: CreateReservationDTO) {
        val book = bookRepository.getObject(reservation.bookId)
        val user = userRepository.getObject(reservation.sessionId)
        val reservation = Reservation(
            book = book,
            user = user,
            pickUpDate = reservation.pickUpDate,
            dropOffDate = reservation.dropOffDate
        )
        reservation.validate()
        if (!canReserve(reservation)) throw BusinessException("Reserva no disponible en esa fecha")
        reservationRepository.create(reservation)
        user.addBibliokarmas(book.calculateBibliokarmas(reservation.reservationDays(), user.bibliokarmas))
        book.addReservation(reservation.id)
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
            totalPages = ceil(filtered.size.toDouble() / pageSize).toInt()
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
            totalPages = ceil(filtered.size.toDouble() / pageSize).toInt()
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

    fun getUserLentBooksNumber(userId: Int): Int =
        reservationRepository.repositoryObjects().filter { reservation ->
            reservation.bookOwnerId() == userId && (reservation.state == State.ACTIVE || reservation.state == State.BORROWED || reservation.state == State.SOON_TO_END)}.size


    fun orchestrateFilterAndSortBooks(userId: Int, page: Int, pageSize: Int, filterCriteria: FilterCriteria, sortCriteria: SortCriteria): PagedResult<ReservationProfileDTO> {
        val userBooksIntoReservations: List<Reservation> = this.getUserOwnBooksIntoReservations(userId)
        return reservationRepository.getFilteredAndSortedReservations(userId, userBooksIntoReservations, page, pageSize, filterCriteria, sortCriteria)
    }

    private fun generateEmptyReservationsForNotReservedBooks(books: List<Book>): List<Reservation> {
        return books.map { book ->
            Reservation(
                book = book,
                pickUpDate = LocalDate.of(1000, 1, 1),
                dropOffDate = LocalDate.of(1000, 2, 1)
            )
        }
    }

    fun getUserOwnBooksIntoReservations(userId: Int): List<Reservation> {
        val userOwnBookIds: List<Book> = bookRepository.findAllByUserId(userId)
        val notReservedBooksIds: List<Book> = reservationRepository.filterNoReservedBooks(userOwnBookIds)

        return generateEmptyReservationsForNotReservedBooks(notReservedBooksIds)
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

}