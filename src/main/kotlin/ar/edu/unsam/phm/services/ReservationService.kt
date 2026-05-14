package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.dto.PagedResult
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.ReservedPeriodDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.*
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
    val mongoReservationRepository: MongoReservationRepository,
    @Autowired
    val bookRepository: MongoBookRepository,
    @Autowired
    val userRepository: CrudUserRepository,
    @Autowired
    val reviewRepository: CrudReviewRepository,
    @Autowired
    val mongoReservationService: MongoReservationRepository,
) {
    @Transactional
    fun createReservation(reservation: CreateReservationDTO) {
        val book = bookRepository.findById(reservation.bookId)
            .orElseThrow { NotFoundException("No existe el libro con id: ${reservation.bookId}") }

        val user = userRepository.findById(reservation.sessionId)
            .orElseThrow { NotFoundException("No existe el usuario con id: ${reservation.sessionId}") }

        val newReservation = Reservation(
            user = user,
            bookId = book.bookId,
            book = book,
            pickUpDate = reservation.pickUpDate,
            dropOffDate = reservation.dropOffDate
        )

        newReservation.validate()

        if (reservationRepository.hasOverlappingReservation(
                book.id!!,
                reservation.pickUpDate,
                reservation.dropOffDate
            )
        ) {
            throw BusinessException("Reserva no disponible en esa fecha")
        }

        user.addBibliokarmas(book.calculateBibliokarmas(newReservation.reservationDays(), user.bibliokarmas))

        userRepository.save(user)
        reservationRepository.save(newReservation)

        mongoReservationService.save(newReservation.toDoc(book.owner.id))
    }

    // esto quiza esta de mas, supongo que regla de negocio?
    //@Transactional(readOnly = true)
    fun getReservesByUserId(userId: String, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "pickUpDate"))
        // val reservationsPage = reservationRepository.findByLectorIdFiltered(userId, search, UserTypes.PUBLISHER, pageable)
        val reservationsPage = mongoReservationRepository.findByLectorIdFiltered(userId, search, pageable)
        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content)
        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
    }

    //@Transactional(readOnly = true)
    fun getLoansMadeByUserId(userId: String, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "pickUpDate"))
        val reservationsPage = mongoReservationRepository.findByOwnerIdFiltered(userId, search, pageable)
        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content, true)
        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
    }

    private fun getReservationsWithBibliokarmasDTO(
        reservations: List<ReservationDoc>,
        own: Boolean = false
    ): List<ReservationDTO> {
        if (reservations.isEmpty()) return emptyList()

        val reviewMap: Map<String?, Int> = reviewRepository
            .findRatingsByReservationIdIn(reservations.map { it.id })
            .associate { it.reservationId to it.rating }

        val bookMap: Map<String, Book> = bookRepository
            .findAllByBookIdIn(reservations.map { it.bookId })
            .associateBy { it.bookId }

        val userMap: Map<String, User> = userRepository
            .findAllById(reservations.map { it.userId })
            .associateBy { it.id!! }

        return reservations.map { reservation ->
            val rating = reviewMap[reservation.id]
            val book = bookMap[reservation.bookId]
                ?: throw BusinessException("No se encontró el libro ${reservation.bookId}")
            val user = userMap[reservation.userId]
                ?: throw BusinessException("No se encontró el usuario ${reservation.userId}")

            val bibliokarmas = book.calculateBibliokarmas(
                reservation.reservationDays(),
                user.bibliokarmas
            )

            reservation.toDTO(rating != null, book, user).apply {
                if (rating != null) this.review = rating
                this.canRate = !own && rating == null && this.state == State.RETURNED
                this.bibliokarmas = bibliokarmas
            }
        }
    }

    @Transactional
    fun rateLoan(reservationId: String, rating: Int, comment: String, userId: String) {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { BusinessException("Reserva $reservationId no encontrada") }

        val reviewer = userRepository.findById(userId)
            .orElseThrow { BusinessException("Usuario $userId no encontrado") }

        val newReview = Review(
            rating = rating,
            review = comment,
            reviewerName = reviewer.name,
            reservation = reservation,
            bookId = reservation.bookId,
        )
        newReview.validate()

        reviewRepository.save(newReview)
//        reservation.book.addReview(newReview)
    }

//    @Transactional(readOnly = true)
//    fun getUserLentBooksNumber(userId: String): Long = reservationRepository.countUserReservedBooks(userId)

    @Transactional(readOnly = true)
    fun getUserReadBooksNumber(userId: String): Long = reservationRepository.countUserReadBooksNumber(userId)

    fun getReservedDates(bookId: String): List<ReservedPeriodDTO> =
        reservationRepository.findAllByBookId(bookId)
            .map { ReservedPeriodDTO(it.pickUpDate, it.dropOffDate) }
}