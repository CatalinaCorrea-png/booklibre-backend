package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.State
import ar.edu.unsam.phm.dto.ReservationProfileDTO
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
        if (reservation.pickUpDate.isBefore(LocalDate.now()))
            throw BusinessException("La fecha de recogida no puede ser anterior a hoy")
        if (reservation.dropOffDate.isBefore(reservation.pickUpDate))
            throw BusinessException("No se puede reservar un libro si su fecha de devolucion es antes que su recogida")
        if (!canReserve(reservation)) throw BusinessException("Reserva no disponible en esa fecha")
        reservationRepository.create(reservation)

        // Acá sumo la reserva al libro?????
        val book = bookRepository.getObject(reservation.book.id)
        book.addReservation(reservation.id)
        reservation.state = State.BORROWED
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

    private fun filterBooksInReservationListBy(filterCrit: String, bookList: List<ReservationProfileDTO>): List<ReservationProfileDTO> {
        if (filterCrit == "Todos") {
            return bookList
        } else {
            return bookList.filter { reserve ->
    //            tengo que hacer esto xq hay mas estados de los que tengo que manejar
                if (reserve.state == "Proximo a vencer") reserve.state = "Prestado"
                else if (reserve.state == "Devuelto") reserve.state = "Disponible"

                reserve.state == filterCrit
            }
        }
    }

/*
*
* Todo por la interfaz de paginado
*
* */

    private fun sortByAscTitle(list: List<ReservationProfileDTO>): List<ReservationProfileDTO> =
        list.sortedBy {it.book.title}

    private fun sortByDescTitle(list: List<ReservationProfileDTO>): List<ReservationProfileDTO> =
        list.sortedByDescending {it.book.title}

    private fun sortByAscDate(list: List<ReservationProfileDTO>): List<ReservationProfileDTO> =
        list.sortedBy {it.book.timestamp}

    private fun sortByDescDate(list: List<ReservationProfileDTO>): List<ReservationProfileDTO> =
        list.sortedByDescending {it.book.timestamp}

    fun filterAndSortUserBooks(bookList: List<ReservationProfileDTO>, filterCrit: String, sortCrit: String): List<ReservationProfileDTO> {
        var filteredBooks = this.filterBooksInReservationListBy(filterCrit, bookList)

        when (sortCrit) {
            "title_asc" -> filteredBooks = this.sortByAscTitle(filteredBooks)
            "title_desc" -> filteredBooks = this.sortByDescTitle(filteredBooks)
            "date_asc" -> filteredBooks = this.sortByAscDate(filteredBooks)
            "date_desc" -> filteredBooks = this.sortByDescDate(filteredBooks)
        }
        return filteredBooks
    }

    fun getBookReviews(bookId: Int, page: Int = 0, pageSize: Int = 2): List<Review> {
        return reservationRepository.repositoryObjects()
            .filter { it.book.id == bookId && it.review.notEmptyReview() }
            .sortedByDescending { it.review.timestamp }
            .drop(page * pageSize)
            .take(pageSize)
            .map { it.review }
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