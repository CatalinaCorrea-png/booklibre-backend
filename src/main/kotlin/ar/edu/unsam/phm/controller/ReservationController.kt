package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.FilterCriteria
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.SortCriteria
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.dto.PagedResult
import ar.edu.unsam.phm.dto.ReservationDTO
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.dto.ReservationProfileDTO
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
    val bookService: BookService,
    val userService: UserService
) {

    // Esto lo hace dana seguro
    @PostMapping("/create-reservation")
    fun createReservation(@RequestBody reservationDTO: CreateReservationDTO) {
        val book = bookService.getBookById(reservationDTO.bookId)
        val user = userService.getUserById(reservationDTO.sessionId)
        val reservation = Reservation(
            book = book,
            user = user,
            pickUpDate = reservationDTO.pickUpDate,
            dropOffDate = reservationDTO.dropOffDate
        )
        reservationService.createReservation(reservation)
    }
    // ESTAS SON LAS RESERVAS QUE VOS HICISTE
    @GetMapping("/lector/{userId}")
    fun getReservesByUserId(
        @PathVariable userId: Int,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam page: Int,
        @RequestParam pageSize: Int): PagedResult<ReservationDTO> =
        reservationService.getReservesByUserId(userId, search, page, pageSize)

    // ESTAS SON LAS RESERVAS QUE TE HICIERON A VOS
    @GetMapping("/owner/{userId}")
    fun getLoansMadeByUserId(
        @PathVariable userId: Int,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam page: Int,
        @RequestParam pageSize: Int): PagedResult<ReservationDTO>  =
        reservationService.getLoansMadeByUserId(userId, search, page, pageSize)

    @PatchMapping("/{reservationId}/calificar")
    fun rateLoan(@PathVariable reservationId: Int, @RequestBody body: ReviewDTO, @RequestParam userId: Int) {
        reservationService.rateLoan(reservationId, body.rating, body.review, userId)
    }

    @GetMapping("/userOwnBooks/{userId}")
    fun getUserOwnBooks(
        @PathVariable userId: Int,
        @RequestParam(defaultValue = "ALL") filterCriteria: FilterCriteria,
        @RequestParam(defaultValue = "DATE_DESC") sortCriteria: SortCriteria
    ): List<ReservationProfileDTO> {
        val userOwnBooks: List<Reservation> = reservationService.getUserOwnBooks(userId)
        val userOwnBooksDTOs: List<ReservationProfileDTO> = userOwnBooks.map { it.toReservationProfileDTO() }
        val filteredAndSortedBooks: List<ReservationProfileDTO> = reservationService.filterAndSortUserBooks(userOwnBooksDTOs, filterCriteria, sortCriteria)
        return filteredAndSortedBooks
    }

    @GetMapping("/userReadBooks/{userId}")
    fun getUserReadBooks(@PathVariable userId: Int): Int =
        reservationService.getUserReservationsNumber(userId)

    @GetMapping("/userLentBooks/{userId}")
    fun getUserLentBooks(@PathVariable userId: Int): Int =
        reservationService.getUserLentBooksNumber(userId)

    @GetMapping("/book-review/{bookId}")
    fun getBookReviews(@PathVariable bookId: Int, @RequestParam page: Int, @RequestParam pageSize: Int): List<ReviewDTO> =
        reservationService.getBookReviews(bookId, page, pageSize).map { it.toDTO() }

//    @GetMapping("/book-review/{bookId}/average")
//    fun getBookAverageRating(@PathVariable bookId: Int): Double =
//        reservationService.getBookAverageRating(bookId)
}