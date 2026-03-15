package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.BookSearchCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.PageResponse
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.services.ReservationService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class ReservationController(
    val reservationService: ReservationService
) {

    // Esto lo hace dana seguro
//    @PostMapping("/create-reservation")
//    fun createReservation(@RequestBody reservationDTO: ReservationDTO) {
//        val reservation = reservationDTO.fromDTO()
//        return reservationService.createReservation(reservation)
//    }

    /* SE OCUPA EL BOOK CONTROLLER
    @PostMapping("/filtered-reservations")
    fun getFilteredReservations(@RequestBody bookSearchCriteria: BookSearchCriteria): PageResponse<ReservationDTO> {
        return reservationService.getAvailableReservationsBy(bookSearchCriteria)
    }
     */


}