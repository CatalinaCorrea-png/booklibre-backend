package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.services.ReservationService
import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class ReservationController(
    val reservationService: ReservationService,
    val bookService: BookService,
    val userService: UserService
) {

    // Esto lo hace dana seguro
    @PostMapping("/create-reservation")
    fun createReservation(@RequestBody reservationDTO: CreateReservationDTO) {
        val book = bookService.getBookById(reservationDTO.bookId)
        val user = userService.getUserById(reservationDTO.userId)
        val reservation = Reservation(
            book = book,
            user = user,
            pickUpDate = reservationDTO.pickUpDate,
            dropOffDate = reservationDTO.dropOffDate
        )
        reservationService.createReservation(reservation)
    }

    /* SE OCUPA EL BOOK CONTROLLER
    @PostMapping("/filtered-reservations")
    fun getFilteredReservations(@RequestBody bookSearchCriteria: BookSearchCriteria): PageResponse<ReservationDTO> {
        return reservationService.getAvailableReservationsBy(bookSearchCriteria)
    }
     */


}