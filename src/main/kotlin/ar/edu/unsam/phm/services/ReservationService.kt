package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.PagedResult
import ar.edu.unsam.phm.dto.ProfilePageable
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.toReservationProfileDTO
import ar.edu.unsam.phm.repository.CrudBookRepository
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.ceil

@Service
class ReservationService(
    val reservationRepository: CrudReservationRepository,
    val bookRepository: CrudBookRepository,
    val userRepository: UserRepository
){
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
//
//    fun getReservesByUserId(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
//        val result = reservationRepository.findByLectorIdFiltered(userId, search, page, pageSize)
//
//        val reservationsDTOs = getReservationsWithBibliokarmasDTO(result.items)
//
//        return PagedResult(
//            items = reservationsDTOs,
//            total = result.total,
//            totalPages = result.totalPages
//        )
//    }
//
//    fun getLoansMadeByUserId(userId: Long, search: String, page: Int, pageSize: Int): PagedResult<ReservationDTO> {
//        val result = reservationRepository.findByOwnerIdFiltered(userId, search, page, pageSize)
//
//        val reservationsDTOs = getReservationsWithBibliokarmasDTO(result.items)
//
//        return PagedResult(
//            items = reservationsDTOs,
//            total = result.total,
//            totalPages = result.totalPages
//        )
//    }
//
//    fun getReservationsWithBibliokarmasDTO(result: List<Reservation>) : List<ReservationDTO> {
//        return result.map { reservation ->
//            val bookReservationsNumber = reservationRepository.findByBookId(reservation.book.id!!).size
//            val reservationDTO = reservation.toDTO()
//            reservationDTO.bibliokarmas = reservation.book.calculateBibliokarmas(reservation.reservationDays(), reservation.user.bibliokarmas, bookReservationsNumber)
//            reservationDTO
//        }
//    }
//
//    fun rateLoan(reservationId: Long, puntuacion: Int, comentario: String, userId: Long) {
//        val reservation = reservationRepository.getObject(reservationId)
//
//        // le pongo la review desde aca, no se si esta bien
//        reservation.review.apply {
//            this.rating = puntuacion
//            this.review = comentario
//            this.reviewerName = userRepository.getObject(userId).name
//        }
//
//        //! acordate de actualizarlo bobo
//        reservationRepository.update(reservation)
//    }
//
    fun getUserLentBooksNumber(userId: Long): Long = reservationRepository.countUserReservedBooks(userId)

    fun getUserReadBooksNumber(userId: Long): Long = reservationRepository.countUserReadBooksNumber(userId)

    fun filteredAndSortReservations(reservations: List<Reservation>, pageableObject: ProfilePageable): PagedResult<ReservationProfileDTO> {

        //         Spring Data JPA parses all method names in a repository interface to derive queries,
        //         including default methods. The name filterAndSortReservations was parsed as a query for a filter property, which doesn't exist on Reservation.

        val filteredAndSortedList: List<ReservationProfileDTO> = reservations
            .filter(pageableObject.filterCriteria.predicate)
            .sortedWith(pageableObject.sortCriteria.comparator)
            .map { it.toReservationProfileDTO() }

        return PagedResult(
            items = filteredAndSortedList.drop(pageableObject.page * pageableObject.pageSize).take(pageableObject.pageSize),
            total = filteredAndSortedList.size,
            totalPages = ceil(filteredAndSortedList.size.toDouble() / pageableObject.pageSize).toInt()
        )
    }

    fun orchestrateFilterAndSortBooks(userId: Long, pageableObject: ProfilePageable): PagedResult<ReservationProfileDTO> {
        val fictitiousReservations: List<Reservation> = this.getUserOwnBooksIntoReservations(userId)
        val realReservations: List<Reservation> = this.getUserReservedBooks(userId)
        val merged: List<Reservation> = this.sortAndDistinct(realReservations + fictitiousReservations)
        return filteredAndSortReservations(merged, pageableObject) // -> armar un objeto con los ultimos 4 parametros
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

    private fun sortAndDistinct(reservations: List<Reservation>): List<Reservation> =
        reservations.sortedByDescending { it.pickUpDate }.distinctBy { it.book.id }

    fun getUserOwnBooksIntoReservations(userId: Long): List<Reservation> {
        val notReservedBooks: List<Book> = this.getUserNotReservedBooks(userId)
        return this.generateEmptyReservationsForNotReservedBooks(notReservedBooks)
    }

    fun getUserNotReservedBooks(userId: Long): List<Book> =
        bookRepository
            .findAllBooksWithoutReservations(userId)

    fun getUserReservedBooks(userId: Long): List<Reservation> =
        reservationRepository
            .findAllByBookOwnerId(userId)

//    fun filterNoReservedBooks(booksIds: List<Long>) {
//        reservationRepository
//            .filterNoReservedBooks(booksIds)
//    }

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