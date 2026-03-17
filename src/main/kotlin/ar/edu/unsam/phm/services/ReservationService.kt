package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.ReviewDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.dto.toReservationProfileDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ReservationService(
    val reservationRepository: ReservationRepository,
    val bookRepository: BookRepository
){
    fun createReservation(reservation: Reservation) {
        if (!canReserve(reservation)) throw BusinessException("Reserva no disponible en esa fecha")
        reservationRepository.create(reservation)

        // Acá sumo la reserva al libro?????
        val book = bookRepository.getObject(reservation.book.id)
        book.addReservation(reservation.id)
    }

    // esto tiene que estar negado asi devuelve true si no hay solapamiento
    fun canReserve(reservation: Reservation) : Boolean = reservationRepository.repositoryObjects().any { !it.dateOverlaps(reservation) }

    fun getAvailableReservations(reservation: Reservation) : MutableList<Reservation> {
        return reservationRepository.repositoryObjects().filter { it.dateOverlaps(reservation) }.toMutableList()
    }

    fun getReservesByUserId(userId: Int, search: String): List<Reservation> {
        return reservationRepository.findByLectorId(userId).filter { res ->
            res.book.title.contains(search, ignoreCase = true) ||
                    res.book.author.name.contains(search, ignoreCase = true)
        }
    }

    fun getLoansMadeByUserId(userId: Int, search: String): List<Reservation> {
        return reservationRepository.findByOwnerId(userId).filter { res ->
            res.book.title.contains(search, ignoreCase = true) ||
                    res.book.author.name.contains(search, ignoreCase = true)
        }
    }

    fun rateLoan(reservationId: Int, puntuacion: Int, comentario: String) {
        val reservation = reservationRepository.getObject(reservationId)

        // le pongo la review desde aca, no se si esta bien
        reservation.review.apply {
            this.rating = puntuacion
            this.review = comentario
        }

        //! acordate de actualizarlo bobo
        reservationRepository.update(reservation)
    }

    fun getUserOwnBooks(userId: Int): List<ReservationProfileDTO> {

        val everyUserOwnBook: List<Book> =
            bookRepository.repositoryObjects().filter { book -> book.owner.id == userId }

        val reservationsWithBooksOwnByUser =
            reservationRepository.repositoryObjects().filter { reserve -> reserve.bookOwnerId() == userId }

        val userNotReservedBooks = everyUserOwnBook.filter { book -> reservationsWithBooksOwnByUser.none { reservation -> reservation.book.id == book.id} }

        val emptyReservationsForNotReservedBooks = userNotReservedBooks.map { book -> Reservation(book = book, pickUpDate = LocalDate.of(1000, 1, 1), dropOffDate = LocalDate.of(1000, 2, 1)) }

        val reservationsDTOs = reservationsWithBooksOwnByUser.map { it.toReservationProfileDTO() } + emptyReservationsForNotReservedBooks.map { it.toReservationProfileDTO() }

        return reservationsDTOs
    }

    fun getUserReservationsNumber(userId: Int): Int =
        reservationRepository.repositoryObjects().filter { reservation ->
            reservation.holderId() == userId && reservation.dropOffDate.isBefore(LocalDate.now()) }.size

    fun getBookReviews(bookId: Int, page: Int = 0, pageSize: Int = 2): List<ReviewDTO> {
        return reservationRepository.repositoryObjects()
            .filter { it.book.id == bookId && it.review.notEmptyReview() }
            .sortedByDescending { it.review.timestamp }
            .drop(page * pageSize)
            .take(pageSize)
            .map { it.review.toDTO() }
    }

//    fun getBookAverageRating(bookId: Int): Double {
//        val reviews = reservationRepository.repositoryObjects()
//            .filter { it.book.id == bookId }
//            .filter { it.review.rating > 0 }
//            .map { it.review.rating }
//
//        return if (reviews.isEmpty()) 0.0 else reviews.average()
//    }

}