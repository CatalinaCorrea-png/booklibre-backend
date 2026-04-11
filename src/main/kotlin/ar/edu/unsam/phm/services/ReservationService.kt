package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.repository.CrudBookRepository
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.ceil

@Service
class ReservationService(
    val reservationRepository: CrudReservationRepository,
    val bookRepository: CrudBookRepository,
    val userRepository: CrudUserRepository,
) {
//    fun createReservation(reservation: CreateReservationDTO) {
//        val book = bookRepository.getObject(reservation.bookId)
//        val user = userRepository.getObject(reservation.sessionId)
//        val reservation = Reservation(
//            book = book,
//            user = user,
//            pickUpDate = reservation.pickUpDate,
//            dropOffDate = reservation.dropOffDate
//        )
//        reservation.validate()
//        if (!reservationRepository.hasOverlappingReservation(book.id!!, reservation)) {
//            throw BusinessException("Reserva no disponible en esa fecha")
//        }
//        reservationRepository.create(reservation)
//        user.addBibliokarmas(book.calculateBibliokarmas(reservation.reservationDays(), user.bibliokarmas))
//        book.addReservation(reservation.id!!)
//    }

    // esto quiza esta de mas, supongo que regla de negocio?
    @Transactional(readOnly = true)
    fun getReservesByUserId(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "pickUpDate"))
        val reservationsPage = reservationRepository.findByLectorIdFiltered(userId, search, pageable)
        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content)
        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
    }

    @Transactional(readOnly = true)
    fun getLoansMadeByUserId(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "pickUpDate"))
        val reservationsPage = reservationRepository.findByOwnerIdFiltered(userId, search, pageable)
        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content)
        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
    }

    private fun getReservationsWithBibliokarmasDTO(reservations: List<Reservation>): List<ReservationDTO> {
        val bookIds = reservations.map { it.book.id!! }
        val countMap = reservationRepository.countByBookIds(bookIds)
            .associate { row -> (row[0] as Long) to (row[1] as Long) }
        // todo: segun dodine el mapeo lo hace el controller
        return reservations.map { reservation ->
            val numReservations = countMap[reservation.book.id!!] ?: 0L
            reservation.toDTO().apply {
                bibliokarmas = reservation.book.calculateBibliokarmas(
                    reservation.reservationDays(),
                    reservation.user.bibliokarmas,
                    numReservations.toInt()
                )
            }
        }
    }

    @Transactional
    fun rateLoan(reservationId: Long, rating: Int, comment: String, userId: Long) {
        val reservation = reservationRepository.findById(reservationId).get()
        //.orElseThrow { NotFoundException("Reserva $reservationId no encontrada") }
        val reviewer = userRepository.findById(userId).get()
        //.orElseThrow { NotFoundException("Usuario $userId no encontrado") }

        reservation.review = Review(
            rating = rating,
            review = comment,
            reviewerName = reviewer.name
        )
    }

    @Transactional(readOnly = true)
    fun getUserLentBooksNumber(userId: Long): Long = reservationRepository.countUserReservedBooks(userId)

    @Transactional(readOnly = true)
    fun getUserReadBooksNumber(userId: Long): Long = reservationRepository.countUserReadBooksNumber(userId)

//    fun getBookReviews(bookId: Long, page: Int = 0, pageSize: Int = 2): List<Review> {
//        return reservationRepository.findReviewsByBookId(bookId)
//            .drop(page * pageSize)
//            .take(pageSize)
//            .map { it.review }
//    }
//
//    fun getReservedDates(bookId: Long): List<ReservedPeriodDTO> =
//        reservationRepository.findByBookId(bookId)
//            .map { ReservedPeriodDTO(it.pickUpDate, it.dropOffDate) }
}