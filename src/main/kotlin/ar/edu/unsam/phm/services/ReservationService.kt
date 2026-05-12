package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.State
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.domain.toDoc
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.CrudReviewRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
import ar.edu.unsam.phm.repository.MongoReservationRepository
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
            bookId = book.id!!,
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
//    fun getReservesByUserId(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
//        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "pickUpDate"))
//        val reservationsPage = reservationRepository.findByLectorIdFiltered(userId, search, UserTypes.PUBLISHER,pageable)
//        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content)
//        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
//    }

    //@Transactional(readOnly = true)
//    fun getLoansMadeByUserId(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
//        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "pickUpDate"))
//        val reservationsPage = reservationRepository.findByOwnerIdFiltered(userId, search, UserTypes.READER, pageable)
//        val reservationsDTOs = getReservationsWithBibliokarmasDTO(reservationsPage.content, true)
//        return PagedResult(reservationsDTOs, reservationsPage.size, reservationsPage.totalPages)
//    }

    private fun getReservationsWithBibliokarmasDTO(
        reservations: List<Reservation>,
        own: Boolean = false
    ): List<ReservationDTO> {
        if (reservations.isEmpty()) return emptyList()

        val reviewMap: Map<String?, Int> = reviewRepository
            .findRatingsByReservationIdIn(reservations.map { it.id!! })
            .associate { it.reservationId to it.rating }

        return reservations.map { reservation ->
            val rating = reviewMap[reservation.id]

            val bibliokarmas = reservation.book?.calculateBibliokarmas(
                reservation.reservationDays(),
                reservation.user.bibliokarmas
            )

            reservation.toDTO(rating != null).apply {
                if (rating != null) {  // el .toDTO se lo pone en 0
                    this.review = rating
                }
                this.canRate = !own && rating == null && this.state == State.RETURNED
                this.bibliokarmas = bibliokarmas!!
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