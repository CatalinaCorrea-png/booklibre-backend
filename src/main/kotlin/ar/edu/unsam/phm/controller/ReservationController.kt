package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.dto.ReservationProfileDTO
import ar.edu.unsam.phm.dto.ReviewDTO
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
        println(reservationDTO.toString())
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
    fun getReservesByUserId(@PathVariable userId: Int, @RequestParam(defaultValue = "") search: String) =
        reservationService.getReservesByUserId(userId, search)

    // ESTAS SON LAS RESERVAS QUE TE HICIERON A VOS
    @GetMapping("/owner/{userId}")
    fun getLoansMadeByUserId(@PathVariable userId: Int, @RequestParam(defaultValue = "") search: String) =
        reservationService.getLoansMadeByUserId(userId, search)

    @PatchMapping("/{reservationId}/calificar")
    fun rateLoan(@PathVariable reservationId: Int, @RequestBody body: ReviewDTO) {
        reservationService.rateLoan(reservationId, body.rating, body.review)
    }

    @GetMapping("/userOwnBooks/{userId}")
    fun getUserOwnBooks(@PathVariable userId: Int): List<ReservationProfileDTO> =
        reservationService.getUserOwnBooks(userId)

    @GetMapping("/userReadBooks/{userId}")
    fun getUserReadBooks(@PathVariable userId: Int): Int =
        reservationService.getUserReservationsNumber(userId)

    @GetMapping("/book-review/{bookId}")
    fun getBookReviews(@PathVariable bookId: Int): List<ReviewDTO> =
        reservationService.getBookReviews(bookId)

//    @GetMapping("/book-review/{bookId}/average")
//    fun getBookAverageRating(@PathVariable bookId: Int): Double =
//        reservationService.getBookAverageRating(bookId)
}