package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.toOwnerDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.LocalDate
import java.util.Optional

class ReviewSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val bookRepository             = mockk<MongoBookRepository>(relaxed = true)
    val mongoReservationRepository = mockk<MongoReservationRepository>(relaxed = true)
    val reservationRepository      = mockk<CrudReservationRepository>(relaxed = true)
    val userRepository             = mockk<CrudUserRepository>(relaxed = true)
    val authorRepository           = mockk<CrudAuthorRepository>(relaxed = true)
    val reviewRepository           = mockk<CrudReviewRepository>()

    val bookService = BookService(
        bookRepository, reservationRepository, mongoReservationRepository, userRepository, authorRepository, reviewRepository
    )
    val reservationService = ReservationService(
        reservationRepository, mongoReservationRepository, bookRepository, userRepository, reviewRepository
    )

    val owner = User(name = "Tolkien", userType = UserTypes.PUBLISHER).apply { id = "owner-id-1" }
    val reader = User(name = "Juan", userType = UserTypes.READER).apply { id = "reader-id-1" }

    val book = Common().apply {
        id = "book-id-1"
        title = "El Señor de los Anillos"
        numPages = 500
        this.owner = owner.toOwnerDTO()
        author = Author("J.R.R. Tolkien", "")
        desc = "Descripcion"
        isbn = "978-0-618-00224-4"
        editorial = "Editorial"
        bookId = "book-logical-id-1"
    }

    val reservation = Reservation(
        user = reader,
        book = book,
        bookId = book.bookId,
        pickUpDate = LocalDate.of(2026, 1, 1),
        dropOffDate = LocalDate.of(2026, 1, 10)
    ).apply { id = "reservation-id-1" }

    describe("getBookReviews") {
        it("Caso feliz: devuelve las reseñas del libro") {
            val review = Review(
                reviewerName = "Juan",
                rating = 5,
                review = "Excelente libro",
                bookId = book.bookId,
                reservation = reservation,
                timestamp = LocalDate.of(2026, 2, 1)
            )
            every { bookRepository.findByBookId(book.bookId) } returns Optional.of(book)
            every { reviewRepository.findAllByBookId(book.bookId) } returns listOf(review)
            every { bookRepository.findByBookId(book.bookId) } returns Optional.of(book)
            val result = bookService.getBookReviews(book.bookId, 0, 10)

            result.size shouldBe 1
            result.first().rating shouldBe 5
        }

        it("Caso triste: devuelve lista vacía cuando el libro no tiene reseñas") {
            every { bookRepository.findByBookId("id-inexistente") } returns Optional.empty()
            every { reviewRepository.findAllByBookId("id-inexistente") } returns emptyList()
            every { bookRepository.findByBookId(book.bookId) } returns Optional.of(book)
            val result = bookService.getBookReviews("id-inexistente", 0, 10)

            result.shouldBeEmpty()
        }
    }

    describe("rateLoan") {
        it("Caso feliz: guarda la reseña con un rating válido") {
            every { reservationRepository.findById("reservation-id-1") } returns Optional.of(reservation)
            every { userRepository.findById("reader-id-1") } returns Optional.of(reader)
            every { reviewRepository.save(any()) } answers { firstArg<Review>() }
            every { bookRepository.findByBookId(book.bookId) } returns Optional.of(book)
            every { reviewRepository.findAllByBookId(book.bookId) } returns listOf()
            every { bookRepository.save(any()) } answers { firstArg<Book>() }

            reservationService.rateLoan("reservation-id-1", 5, "Muy bueno", "reader-id-1")

            verify { reviewRepository.save(any()) }
        }

        it("Caso triste: lanza BusinessException cuando el rating es inválido (mayor a 5)") {
            every { reservationRepository.findById("reservation-id-1") } returns Optional.of(reservation)
            every { userRepository.findById("reader-id-1") } returns Optional.of(reader)

            shouldThrow<BusinessException> { reservationService.rateLoan("reservation-id-1", 6, "Imposible", "reader-id-1") }
        }

        it("Caso triste: lanza BusinessException cuando la reserva no existe") {
            every { reservationRepository.findById("id-inexistente") } returns Optional.empty()

            shouldThrow<BusinessException> { reservationService.rateLoan("id-inexistente", 5, "Review", "reader-id-1") }
        }
    }
})
