package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.FilterCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.SortCriteria
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.dto.PagedResult
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.ReservedPeriodDTO
import ar.edu.unsam.phm.dto.ReviewDTO
import ar.edu.unsam.phm.dto.toReservationProfileDTO
import ar.edu.unsam.phm.dto.toDTO
import ar.edu.unsam.phm.services.ReservationService
import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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
//    // ESTAS SON LAS RESERVAS QUE VOS HICISTE
//    @GetMapping("/lector/{userId}")
//    fun getReservesByUserId(
//        @PathVariable userId: Long,
//        @RequestParam(defaultValue = "") search: String,
//        @RequestParam page: Int,
//        @RequestParam pageSize: Int): PagedResult<ReservationDTO> =
//        reservationService.getReservesByUserId(userId, search, page, pageSize)
//
//    // ESTAS SON LAS RESERVAS QUE TE HICIERON A VOS
//    @GetMapping("/owner/{userId}")
//    fun getLoansMadeByUserId(
//        @PathVariable userId: Long,
//        @RequestParam(defaultValue = "") search: String,
//        @RequestParam page: Int,
//        @RequestParam pageSize: Int): PagedResult<ReservationDTO>  =
//        reservationService.getLoansMadeByUserId(userId, search, page, pageSize)
//
//    @PatchMapping("/{reservationId}/calificar")
//    fun rateLoan(@PathVariable reservationId: Long, @RequestBody body: ReviewDTO, @RequestParam userId: Long) {
//        reservationService.rateLoan(reservationId, body.rating, body.review, userId)
//    }

    @GetMapping("/userOwnBooks/{userId}")
    fun getUserOwnBooks(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "ALL") filterCriteria: FilterCriteria,
        @RequestParam(defaultValue = "DATE_DESC") sortCriteria: SortCriteria,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "4") pageSize: Int
    ): PagedResult<ReservationProfileDTO> =
        reservationService.orchestrateFilterAndSortBooks(userId, page, pageSize, filterCriteria, sortCriteria)

//    @GetMapping("/userReadBooks/{userId}")
//    fun getUserReadBooks(@PathVariable userId: Long): Int =
//        reservationService.getUserReservationsNumber(userId)
//
//    @GetMapping("/userLentBooks/{userId}")
//    fun getUserLentBooks(@PathVariable userId: Long): Int =
//        reservationService.getUserLentBooksNumber(userId)
//
//    @GetMapping("/book-review/{bookId}")
//    fun getBookReviews(@PathVariable bookId: Long, @RequestParam page: Int, @RequestParam pageSize: Int): List<ReviewDTO> =
//        reservationService.getBookReviews(bookId, page, pageSize).map { it.toDTO() }
//
//    @GetMapping("/reservations/book/{bookId}/dates")
//    fun getReservedDatesByBook(@PathVariable bookId: Long): List<ReservedPeriodDTO> =
//        reservationService.getReservedDates(bookId)
}