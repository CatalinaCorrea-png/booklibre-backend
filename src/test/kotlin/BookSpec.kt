import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import model.Common
import model.Reader
import model.Reservation
import model.User
import java.time.LocalDate


class BookSpec : DescribeSpec ({
    // isolationMode = InstancePerTest

    describe("Testing reservations and bibliokarmas") {

        it("Una reserva de libro comun por 4 dias de usuario nuevo") {
            // Arrange
            val newUser = User(userType = Reader)
            val commonBook = Common()

            // Act
            val reservation = Reservation(user = newUser, pickUpDate = LocalDate.now().minusDays(4), dropOffDate = LocalDate.now())
            newUser.reserveBook(book = commonBook, reservation = reservation)

            // Assert
            commonBook.reservations.size shouldBe 1
            commonBook.calculateBibliokarmas(reservation) shouldBe 20
        }


        it("No se puede reservar un libro que ya esta reservado en esa fecha")


    }
})