
import ar.edu.unsam.phm.domain.Common
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.dto.CreateReservationDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.services.ReservationService
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import java.time.LocalDate

class ReservationSpec: DescribeSpec ({
    isolationMode = IsolationMode.InstancePerTest

    val owner = User(userType = UserTypes.PUBLISHER)
    val reader1 = User(userType = UserTypes.READER)
    val reader2 = User(userType = UserTypes.READER)

    val commonBook = Common().apply {
        title = "1984"
        numPages = 328
        this.owner = owner
    }

    describe("Caso feliz y caso triste cuando quiero reservar un libro") {

        val reserveExisting = Reservation(
            user = reader1,
            book = commonBook,
            pickUpDate = LocalDate.of(2026, 4, 1),
            dropOffDate = LocalDate.of(2026, 4, 10)
        )

        it("No puede reservar un libro prestado en esa fecha") {
            // Arrange
            val reservaRepository = ReservationRepository()
            reservaRepository.create(reserveExisting)
            val bookRepository = BookRepository()
            bookRepository.create(commonBook)
            val userRepository = UserRepository()
            userRepository.create(owner)
            userRepository.create(reader1)
            userRepository.create(reader2)

            val reservationService = ReservationService(reservaRepository, bookRepository, userRepository)

            val newReservationDTO = CreateReservationDTO(
                bookId = commonBook.id,
                sessionId = reader2.id,
                pickUpDate = LocalDate.of(2026, 4, 5),  // se superpone
                dropOffDate = LocalDate.of(2026, 4, 15)
            )

            // Act & Assert
            shouldThrow<BusinessException> { reservationService.createReservation(newReservationDTO) }
        }

        it("Puede reservar un libro en una fecha libre") {
            val reservaRepository = ReservationRepository()
            reservaRepository.create(reserveExisting)
            val bookRepository = BookRepository()
            bookRepository.create(commonBook)
            val userRepository = UserRepository()
            userRepository.create(owner)
            userRepository.create(reader1)
            userRepository.create(reader2)

            val reservationService = ReservationService(reservaRepository, bookRepository, userRepository)

            val newReservationDTO = CreateReservationDTO(
                bookId = commonBook.id,
                sessionId = reader2.id,
                pickUpDate = LocalDate.of(2026, 4, 11),  // no se superpone
                dropOffDate = LocalDate.of(2026, 4, 20)
            )

            shouldNotThrow<BusinessException> { reservationService.createReservation(newReservationDTO) }
        }
    }

})