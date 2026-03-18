import ar.edu.unsam.phm.domain.Common
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserType
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.services.ReservationService
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class BookDetailSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val owner = User(userType = UserType.PUBLISHER)

    val commonBook = Common().apply {
        title = "1984"
        numPages = 328
        this.owner = owner
    }

    describe("Caso feliz y caso triste cuando pido un libro con id = 1"){
        it("Caso feliz: me trae correctamente el libro"){
            // Arrange
            val bookRepository = BookRepository()
            bookRepository.create(commonBook)
            val bookService = BookService(bookRepository, ReservationRepository())

            // Act
            val result = bookService.getBookById(commonBook.id)

            // Assert
            result.title shouldBe "1984"
        }
        it("Caso triste: lanza excepcion si el libro no existe") {
            // Arrange
            val bookRepository = BookRepository()
            val bookService = BookService(bookRepository, ReservationRepository())

            // Act & Assert
            shouldThrow<NotFoundException> { bookService.getBookById(999) }
        }
    }
})