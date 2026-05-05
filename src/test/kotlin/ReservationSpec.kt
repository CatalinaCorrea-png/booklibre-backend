package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.*
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.util.Optional

class ReservationSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val reservationRepository = mockk<CrudReservationRepository>()
    val bookRepository        = mockk<CrudBookRepository>()
    val userRepository        = mockk<CrudUserRepository>()
    val reviewRepository      = mockk<CrudReviewRepository>(relaxed = true)

    val reservationService = ReservationService(
        reservationRepository, bookRepository, userRepository, reviewRepository
    )

    val owner = User(name = "Tolkien", userType = UserTypes.PUBLISHER, bibliokarmas = 0).apply { id = 1L }
    val reader = User(name = "Juan", userType = UserTypes.READER, bibliokarmas = 100).apply { id = 2L }

    val book = Common().apply {
        id = 1L
        title = "El Señor de los Anillos"
        numPages = 500
        this.owner = owner
        author = Author("J.R.R. Tolkien", "")
    }

    describe("createReservation") {

        it("Caso feliz: crea la reserva cuando las fechas están disponibles") {
            val dto = CreateReservationDTO(
                bookId = 1L,
                sessionId = 2L,
                pickUpDate = LocalDate.of(2030, 6, 1),
                dropOffDate = LocalDate.of(2030, 6, 10)
            )

            every { bookRepository.findById(1L) } returns Optional.of(book)
            every { userRepository.findById(2L) } returns Optional.of(reader)
            every { reservationRepository.hasOverlappingReservation(any(), any(), any()) } returns false
            every { userRepository.save(any()) } returns reader
            every { reservationRepository.save(any()) } answers { firstArg() }

            shouldNotThrow<Exception> { reservationService.createReservation(dto) }
        }

        it("Caso triste: lanza BusinessException cuando hay solapamiento de fechas") {
            val dto = CreateReservationDTO(
                bookId = 1L,
                sessionId = 2L,
                pickUpDate = LocalDate.of(2030, 6, 5),
                dropOffDate = LocalDate.of(2030, 6, 15)
            )

            every { bookRepository.findById(1L) } returns Optional.of(book)
            every { userRepository.findById(2L) } returns Optional.of(reader)
            every { reservationRepository.hasOverlappingReservation(any(), any(), any()) } returns true

            shouldThrow<BusinessException> { reservationService.createReservation(dto) }
        }
    }
})