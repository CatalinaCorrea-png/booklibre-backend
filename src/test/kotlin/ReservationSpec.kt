
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

        it("No puede reservar un libro prestado en esa fecha") {
            // Arrange
            val userRepository = UserRepository()
            val reservaRepository = ReservationRepository()
            val bookRepository = BookRepository()

            userRepository.create(reader2)
            bookRepository.create(commonBook)

            val reserveExisting = Reservation(
                user = reader1,
                book = commonBook,
                pickUpDate = LocalDate.of(2026, 4, 1),
                dropOffDate = LocalDate.of(2026, 4, 10)
            )
            reservaRepository.create(reserveExisting)

            val reservationService = ReservationService(reservaRepository, bookRepository, userRepository)

            val newReservation = CreateReservationDTO(
                bookId = commonBook.id,
                sessionId = reader2.id,
                pickUpDate = LocalDate.of(2026, 4, 5), // se superpone con 4/1 - 4/10
                dropOffDate = LocalDate.of(2026, 4, 20)
            )

            // Act & Assert
            shouldThrow<BusinessException> { reservationService.createReservation(newReservation) }
        }

        it("Puede reservar un libro en una fecha libre") {
            // Arrange
            val userRepository = UserRepository()
            val reservaRepository = ReservationRepository()
            val bookRepository = BookRepository()

            userRepository.create(reader2)
            bookRepository.create(commonBook)

            val reserveExisting = Reservation(
                user = reader1,
                book = commonBook,
                pickUpDate = LocalDate.of(2026, 4, 1),
                dropOffDate = LocalDate.of(2026, 4, 10)
            )
            reservaRepository.create(reserveExisting)

            val reservationService = ReservationService(reservaRepository, bookRepository, userRepository)

            val newReservation = CreateReservationDTO(
                bookId = commonBook.id,
                sessionId = reader2.id,
                pickUpDate = LocalDate.of(2026, 4, 11), // no se superpone
                dropOffDate = LocalDate.of(2026, 4, 20)
            )

            // Act & Assert
            shouldNotThrow<BusinessException> { reservationService.createReservation(newReservation) }
        }
    }

})