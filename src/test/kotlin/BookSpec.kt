import errors.BusinessException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import model.*
import java.time.LocalDate


class BookSpec : DescribeSpec ({
    isolationMode = IsolationMode.InstancePerTest

    describe("Testing reservations and bibliokarmas for new User and 4 day reservation") {
        val newUser = User(userType = Reader)
        val reservation = Reservation(user = newUser, pickUpDate = LocalDate.now().minusDays(4), dropOffDate = LocalDate.now())

        it("Una reserva de libro Comun por 4 dias de usuario nuevo") {
            // Arrange
            val commonBook = Common()

            // Act
            newUser.reserveBook(book = commonBook, reservation = reservation)

            // Assert
            commonBook.reservations.size shouldBe 1
            commonBook.calculateBibliokarmas(reservation) shouldBe 20
            newUser.bibliokarmas shouldBe 20
        }

        it("Una reserva de libro con Dedicatoria por 4 dias de usuario nuevo") {
            // Arrange
            val dedicationBook = WithADedication()

            // Act
            newUser.reserveBook(book = dedicationBook, reservation = reservation)

            // Assert
            dedicationBook.reservations.size shouldBe 1
            dedicationBook.calculateBibliokarmas(reservation) shouldBe 230
            newUser.bibliokarmas shouldBe 230
        }

        it("Una reserva de libro Coleccionable por 4 dias de usuario nuevo") {
            // Arrange
            val collectableBook = Collectable()

            // Act
            newUser.reserveBook(book = collectableBook, reservation = reservation)

            // Assert
            collectableBook.reservations.size shouldBe 1
            newUser.bibliokarmas shouldBe 20
        }

        it("Dos reservas de libro Comun por 4 dias de usuario nuevo") {
            // Arrange
            val commonBook = Common()
            val otherReservation = Reservation(user = newUser, pickUpDate = LocalDate.now().minusDays(9), dropOffDate = LocalDate.now().minusDays(5))

            // Act
            newUser.reserveBook(book = commonBook, reservation = otherReservation)
            newUser.reserveBook(book = commonBook, reservation = reservation)

            // Assert
            commonBook.reservations.size shouldBe 2
            newUser.bibliokarmas shouldBe 40
        }

    }

    describe("Reservations validations") {

        it("No se puede reservar un libro que ya esta reservado en esa fecha") {
            // Arrange
            val newUser = User(userType = Reader)
            val commonBook = Common()
            val reservation = Reservation(user = newUser, pickUpDate = LocalDate.now().minusDays(4), dropOffDate = LocalDate.now())
            val otherReservation = Reservation(user = newUser, pickUpDate = LocalDate.now().minusDays(4), dropOffDate = LocalDate.now())

            // Act / Assert
            newUser.reserveBook(book = commonBook, reservation = reservation)
            val excepcion = shouldThrow<BusinessException> {
                newUser.reserveBook(book = commonBook, reservation = otherReservation)
            }

            // excepcion.message shouldBe ("Reserva no disponible en esas fechas")
        }
    }

})