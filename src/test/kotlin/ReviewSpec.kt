package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.domain.PageImpl
import java.time.LocalDate
import java.util.Optional

class ReviewSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    // Mocks compartidos
    val bookRepository = mockk<CrudBookRepository>(relaxed = true)
    val reservationRepository = mockk<CrudReservationRepository>(relaxed = true)
    val userRepository= mockk<CrudUserRepository>(relaxed = true)
    val authorRepository= mockk<CrudAuthorRepository>(relaxed = true)
    val reviewRepository= mockk<CrudReviewRepository>()

    val bookService = BookService(
        bookRepository, reservationRepository, userRepository, authorRepository, reviewRepository
    )
    val reservationService = ReservationService(
        reservationRepository, bookRepository, userRepository, reviewRepository
    )

    val owner = User(name = "Tolkien", userType = UserTypes.PUBLISHER).apply { id = 1L }
    val reader = User(name = "Juan", userType = UserTypes.READER).apply { id = 2L }

    val book = Common().apply {
        id = 1L
        title = "El Señor de los Anillos"
        numPages = 500
        this.owner = owner
        author = Author("J.R.R. Tolkien", "")
    }

    val reservation = Reservation(
        user = reader,
        book = book,
        pickUpDate = LocalDate.of(2026, 1, 1),
        dropOffDate = LocalDate.of(2026, 1, 10)
    ).apply { id = 1L }

    describe("getBookReviews") {
        it("Caso feliz: devuelve las reseñas del libro") {
            val review = Review(
                reviewerName = "Juan",
                rating = 5,
                review = "Excelente libro",
                book = book,
                reservation = reservation,
                timestamp = LocalDate.of(2026, 2, 1)
            )
            every { reviewRepository.findAllByBookId(1L, any()) } returns PageImpl(listOf(review))

            val result = bookService.getBookReviews(1L, 0, 10)

            result.size shouldBe 1
            result.first().rating shouldBe 5
        }

        it("Caso triste: devuelve lista vacía cuando el libro no tiene reseñas") {
            every { reviewRepository.findAllByBookId(999L, any()) } returns PageImpl(emptyList())

            val result = bookService.getBookReviews(999L, 0, 10)

            result.shouldBeEmpty()
        }
    }

    describe("rateLoan") {
        it("Caso feliz: guarda la reseña con un rating válido") {
            every { reservationRepository.findById(1L) } returns Optional.of(reservation)
            every { userRepository.findById(2L) } returns Optional.of(reader)
            every { reviewRepository.save(any()) } answers { firstArg() }

            reservationService.rateLoan(1L, 5, "Muy bueno", 2L)

            verify { reviewRepository.save(any()) }
        }

        it("Caso triste: lanza BusinessException cuando el rating es inválido (mayor a 5)") {
            every { reservationRepository.findById(1L) } returns Optional.of(reservation)
            every { userRepository.findById(2L) } returns Optional.of(reader)

            shouldThrow<BusinessException> { reservationService.rateLoan(1L, 6, "Imposible", 2L) }
        }

        it("Caso triste: lanza BusinessException cuando la reserva no existe") {
            every { reservationRepository.findById(999L) } returns Optional.empty()

            shouldThrow<BusinessException> { reservationService.rateLoan(999L, 5, "Review", 2L) }
        }
    }
})
