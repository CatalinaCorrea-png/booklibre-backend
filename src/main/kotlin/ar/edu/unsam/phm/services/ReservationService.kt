package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.CrudBookRepository
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.CrudReviewRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationService(
    @Autowired
    val reservationRepository: CrudReservationRepository,
    @Autowired
    val bookRepository: CrudBookRepository,
    @Autowired
    val userRepository: CrudUserRepository,
    @Autowired
    val reviewRepository: CrudReviewRepository
) {
    @Transactional
    fun createReservation(reservation: CreateReservationDTO) {
        val book = bookRepository.findById(reservation.bookId)
            .orElseThrow { NotFoundException("No existe el libro con id: ${reservation.bookId}") }

        val user = userRepository.findById(reservation.sessionId)
            .orElseThrow { NotFoundException("No existe el usuario con id: ${reservation.sessionId}") }

        val reservation = Reservation(
            book = book,
            user = user,
            pickUpDate = reservation.pickUpDate,
            dropOffDate = reservation.dropOffDate
        )

        reservation.validate()

        if (reservationRepository.hasOverlappingReservation(
                book.id!!,
                reservation.pickUpDate,
                reservation.dropOffDate
            )
        ) {
            throw BusinessException("Reserva no disponible en esa fecha")
        }

        user.addBibliokarmas(book.calculateBibliokarmas(reservation.reservationDays(), user.bibliokarmas).toInt())

        userRepository.save(user)
        reservationRepository.save(reservation)
    }

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
        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content, true)
        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
    }

    private fun getReservationsWithBibliokarmasDTO(
        reservations: List<Reservation>,
        own: Boolean = false
    ): List<ReservationDTO> {
        val reservationIds = reservations.map { it.id!! }

        val reviewMap = reviewRepository
            .findAllByReservationIdIn(reservationIds)
            .associate { it.reservation.id to it.rating }

        return reservations.map { reservation ->
            val ratingEncontrado = reviewMap[reservation.id]
            val hasReview = ratingEncontrado != null

            reservation.toDTO(hasReview).apply {

                if (hasReview) {
                    this.review = ratingEncontrado!!
                }
                if (own) {
                    canRate = false
                }

                bibliokarmas = reservation.book.calculateBibliokarmas(
                    reservation.reservationDays(),
                    reservation.user.bibliokarmas
                ).toInt()
            }
        }
    }

    @Transactional
    fun rateLoan(reservationId: Long, rating: Int, comment: String, userId: Long) {
        val reservation = reservationRepository.findById(reservationId).get()
        val existingReview = reviewRepository.findByReservationId(reservationId)
        if (existingReview != null) {
            throw BusinessException("La reserva ya tiene una reseña asignada.")
        }
        //.orElseThrow { NotFoundException("Reserva $reservationId no encontrada") }
        val reviewer = userRepository.findById(userId).get()
        //.orElseThrow { NotFoundException("Usuario $userId no encontrado") }

        val newReview = Review(
            rating = rating,
            review = comment,
            reviewerName = reviewer.name,
            reservation = reservation,
            book = reservation.book,
        )

        reviewRepository.save(newReview)

        reservation.book.addReview(newReview)

    }

    @Transactional(readOnly = true)
    fun getUserLentBooksNumber(userId: Long): Long = reservationRepository.countUserReservedBooks(userId)

    @Transactional(readOnly = true)
    fun getUserReadBooksNumber(userId: Long): Long = reservationRepository.countUserReadBooksNumber(userId)

    fun getBookReviews(bookId: Long, page: Int = 0, pageSize: Int = 2): List<Review> {
        return reservationRepository.findAllReviewsByBookId(bookId)
            .drop(page * pageSize)
            .take(pageSize)
    }

    fun getReservedDates(bookId: Long): List<ReservedPeriodDTO> =
        reservationRepository.findAllByBookId(bookId)
            .map { ReservedPeriodDTO(it.pickUpDate, it.dropOffDate) }
}