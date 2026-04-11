package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.ReservationService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class ReservationController(
    val reservationService: ReservationService,
) {
//    @PostMapping("/create-reservation")
//    fun createReservation(@RequestBody reservationDTO: CreateReservationDTO) {
//        reservationService.createReservation(reservationDTO)
//    }

    // ESTAS SON LAS RESERVAS QUE VOS HICISTE
    @GetMapping("/lector/{userId}")
    fun getReservesByUserId(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "4") pageSize: Int
    ): PagedResult<ReservationDTO> =
        reservationService.getReservesByUserId(userId, search, page, pageSize)

    // ESTAS SON LAS RESERVAS QUE TE HICIERON A VOS
    @GetMapping("/owner/{userId}")
    fun getLoansMadeByUserId(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "4") pageSize: Int
    ): PagedResult<ReservationDTO> =
        reservationService.getLoansMadeByUserId(userId, search, page, pageSize)

    @PatchMapping("/{reservationId}/calificar")
    fun rateLoan(@PathVariable reservationId: Long, @RequestBody body: ReviewDTO, @RequestParam userId: Long) {
        reservationService.rateLoan(reservationId, body.rating, body.review, userId)
    }

    @GetMapping("/userReadBooks/{userId}")
    fun getUserReadBooks(@PathVariable userId: Long): Long =
        reservationService.getUserReadBooksNumber(userId)

    @GetMapping("/userLentBooks/{userId}")
    fun getUserLentBooks(@PathVariable userId: Long): Long =
        reservationService.getUserLentBooksNumber(userId)
//
//    @GetMapping("/book-review/{bookId}")
//    fun getBookReviews(@PathVariable bookId: Long, @RequestParam page: Int, @RequestParam pageSize: Int): List<ReviewDTO> =
//        reservationService.getBookReviews(bookId, page, pageSize).map { it.toDTO() }
//
//    @GetMapping("/reservations/book/{bookId}/dates")
//    fun getReservedDatesByBook(@PathVariable bookId: Long): List<ReservedPeriodDTO> =
//        reservationService.getReservedDates(bookId)
}