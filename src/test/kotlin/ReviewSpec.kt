import ar.edu.unsam.phm.domain.Common
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.services.ReservationService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import java.time.LocalDate
import io.kotest.matchers.shouldBe


class ReviewSpec: DescribeSpec ({
    isolationMode = IsolationMode.InstancePerTest

    val owner = User(userType = UserTypes.PUBLISHER)

    val commonBook = Common().apply {
        title = "1984"
        numPages = 328
        this.owner = owner
    }

    val reservation = Reservation(
        user = owner,
        book = commonBook,
        review = Review(
            reviewerName = owner.name,
            rating = 5,
            review = "La mejor historia de venganza jamás escrita. No pude soltarlo.",
            timestamp = LocalDate.of(2026, 2, 15)
        ),
        pickUpDate = LocalDate.of(2026, 1, 22),
        dropOffDate = LocalDate.of(2026, 2, 14),
    )

    describe("Caso feliz y triste cuando pido una reseña"){
        it("Caso feliz: me trae correctamente la reseña") {
            // Arrange
            val reservationRepository = ReservationRepository()
            val bookRepository = BookRepository()

            bookRepository.create(commonBook)
            reservationRepository.create(reservation)
            val reservationService = ReservationService(reservationRepository, bookRepository)

            // Act
            val result = reservationService.getBookReviews(commonBook.id, 0, 10)

            // Assert
            result.first().rating shouldBe 5
        }
        it("Caso triste: no encuentra reseñas para un libro inexistente") {
            val reservationRepository = ReservationRepository()
            val bookRepository = BookRepository()
            val reservationService = ReservationService(reservationRepository, bookRepository)

            val result = reservationService.getBookReviews(999, 0, 10)

            result.size shouldBe 0
        }
    }
})