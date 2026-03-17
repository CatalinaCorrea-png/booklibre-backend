package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.ReviewDTO
import ar.edu.unsam.phm.dto.toReservationProfileDTO
import ar.edu.unsam.phm.services.ReservationService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class ReservationController(private val reservationService: ReservationService) {

    // ESTAS SON LAS RESERVAS QUE VOS HICISTE
    @GetMapping("/lector/{userId}")
    fun getReservesByUserId(@PathVariable userId: Int, @RequestParam(defaultValue = "") search: String) =
        reservationService.getReservesByUserId(userId, search)

    // ESTAS SON LAS RESERVAS QUE TE HICIERON A VOS
    @GetMapping("/owner/{userId}")
    fun getLoansMadeByUserId(@PathVariable userId: Int, @RequestParam(defaultValue = "") search: String) =
        reservationService.getLoansMadeByUserId(userId, search)

    @PatchMapping("/{reservationId}/calificar")
    fun rateLoan(@PathVariable reservationId: Int, @RequestBody body: ReviewDTO) {
        reservationService.rateLoan(reservationId, body.rate, body.comment)
    }

    @GetMapping("/userOwnBooks/{userId}?filterBy={filter}&sortBy={sort}")
    fun getUserOwnBooks(@PathVariable userId: Int): List<ReservationProfileDTO> {
        val userOwnBooks: List<Reservation> = reservationService.getUserOwnBooks(userId)
// ->       val filteredAndSortedBooks: List<Reservation> = reservationService.filterAndSortUserBooks(userOwnBooks, filterCriteria, sortCriteria)
        return userOwnBooks.map { it.toReservationProfileDTO() }
    }

    @GetMapping("/userReadBooks/{userId}")
    fun getUserReadBooks(@PathVariable userId: Int): Int =
        reservationService.getUserReservationsNumber(userId)
}