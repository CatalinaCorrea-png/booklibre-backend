import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.repository.BookRepository
import ar.edu.unsam.phm.repository.ReservationRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.services.ReservationService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldContainAll
import java.time.LocalDate

class ProfileSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val owner = User(name = "Carlos")
    val reader1 = User(name = "Maria")
    val reader2 = User(name = "Juan")

    val bookA = Common().apply {
        title = "1984"
        numPages = 328
        this.owner = owner
    }

    val bookB = Common().apply {
        title = "Rayuela"
        numPages = 600
        this.owner = owner
    }

    val bookC = Common().apply {
        title = "El Aleph"
        numPages = 150
        this.owner = owner
    }

    describe("getUserOwnBooks devuelve todos los libros del usuario dentro de una reserva, y se queda con la mas cercana") {

        it("Cada libro aparece exactamente una vez, envuelto en una reserva, y se queda con la de pickUpDate mas reciente") {
            val reservationRepository = ReservationRepository()
            val bookRepository = BookRepository()
            val userRepository = UserRepository()

            userRepository.create(owner)
            userRepository.create(reader1)
            userRepository.create(reader2)

            bookRepository.create(bookA)
            bookRepository.create(bookB)
            bookRepository.create(bookC)

            // Book A: 2 reservas (una vieja, una futura) -> debe quedarse con la futura (pickUpDate mas reciente)
            val reservaPasadaBookA = Reservation(
                user = reader1,
                book = bookA,
                pickUpDate = LocalDate.of(2025, 1, 1),
                dropOffDate = LocalDate.of(2025, 1, 10)
            )
            val reservaFuturaBookA = Reservation(
                user = reader2,
                book = bookA,
                pickUpDate = LocalDate.of(2026, 6, 1),
                dropOffDate = LocalDate.of(2026, 6, 15)
            )

            // Book B: 1 reserva activa (hoy esta entre pickup y dropoff)
            val reservaActivaBookB = Reservation(
                user = reader1,
                book = bookB,
                pickUpDate = LocalDate.now().minusDays(3),
                dropOffDate = LocalDate.now().plusDays(10)
            )

            reservationRepository.create(reservaPasadaBookA)
            reservationRepository.create(reservaFuturaBookA)
            reservationRepository.create(reservaActivaBookB)

            // Book C: sin reservas

            val reservationService = ReservationService(reservationRepository, bookRepository, userRepository)

            // Act
            val result = reservationService.getUserOwnBooks(owner.id)

            // Assert
            result shouldHaveSize 3

            val bookIds = result.map { it.book.id }
            bookIds shouldContainAll listOf(bookA.id, bookB.id, bookC.id)

            // Book A: se quedo con la reserva futura (pickUpDate mas reciente)
            val reservationForBookA = result.find { it.book.id == bookA.id }!!
            reservationForBookA.pickUpDate shouldBe LocalDate.of(2026, 6, 1)

            // Book B: tiene la reserva activa
            val reservationForBookB = result.find { it.book.id == bookB.id }!!
            reservationForBookB.state shouldBe State.ACTIVE

            // Book C: no tiene reservas, se crea una placeholder con estado RETURNED (fecha year 1000)
            val reservationForBookC = result.find { it.book.id == bookC.id }!!
            reservationForBookC.pickUpDate shouldBe LocalDate.of(1000, 1, 1)
            reservationForBookC.state shouldBe State.RETURNED
        }
    }

    describe("Un libro sin reservas previas, al ser reservado para hoy, aparece ACTIVE en el perfil") {

        it("La reserva devuelta tiene estado ACTIVE y fechas reales, no placeholder") {
            // Arrange
            val reservationRepository = ReservationRepository()
            val bookRepository = BookRepository()
            val userRepository = UserRepository()

            userRepository.create(owner)
            userRepository.create(reader1)
            bookRepository.create(bookA)

            val reservaHoy = Reservation(
                user = reader1,
                book = bookA,
                pickUpDate = LocalDate.now(),
                dropOffDate = LocalDate.now().plusDays(7)
            )
            reservationRepository.create(reservaHoy)

            val reservationService = ReservationService(reservationRepository, bookRepository, userRepository)

            // Act
            val result = reservationService.getUserOwnBooks(owner.id)

            // Assert
            result shouldHaveSize 1

            val reservation = result.first()
            reservation.book.id shouldBe bookA.id
            reservation.state shouldBe State.ACTIVE
            reservation.pickUpDate shouldBe LocalDate.now()
            reservation.dropOffDate shouldBe LocalDate.now().plusDays(7)
        }
    }

    describe("El filtro de perfil clasifica correctamente DISPONIBLE vs PRESTADO") {

        it("BORROWED muestra solo libros actualmente prestados, AVAILABLE los disponibles, ALL todos") {
            // Arrange
            val reservationRepository = ReservationRepository()
            val bookRepository = BookRepository()
            val userRepository = UserRepository()

            userRepository.create(owner)
            userRepository.create(reader1)
            userRepository.create(reader2)

            bookRepository.create(bookA)
            bookRepository.create(bookB)
            bookRepository.create(bookC)

            // Book A: reserva activa (PRESTADO)
            val reservaActiva = Reservation(
                user = reader1,
                book = bookA,
                pickUpDate = LocalDate.now().minusDays(3),
                dropOffDate = LocalDate.now().plusDays(10)
            )

            // Book B: reserva pasada (DEVUELTO -> DISPONIBLE)
            val reservaPasada = Reservation(
                user = reader2,
                book = bookB,
                pickUpDate = LocalDate.of(2025, 1, 1),
                dropOffDate = LocalDate.of(2025, 1, 10)
            )

            reservationRepository.create(reservaActiva)
            reservationRepository.create(reservaPasada)

            // Book C: sin reservas (DISPONIBLE)

            val reservationService = ReservationService(reservationRepository, bookRepository, userRepository)

            // Act & Assert: FilterCriteria.ALL -> 3 libros
            val allBooks = reservationService.orchestrateFilterAndSortBooks(
                owner.id, 0, 10, FilterCriteria.ALL, SortCriteria.DATE_DESC
            )
            allBooks.total shouldBe 3

            // FilterCriteria.BORROWED -> solo Book A (ACTIVE)
            val borrowedBooks = reservationService.orchestrateFilterAndSortBooks(
                owner.id, 0, 10, FilterCriteria.BORROWED, SortCriteria.DATE_DESC
            )
            borrowedBooks.total shouldBe 1
            borrowedBooks.items.first().book.title shouldBe "1984"

            // FilterCriteria.AVAILABLE -> Book B (RETURNED) y Book C (sin reserva)
            val availableBooks = reservationService.orchestrateFilterAndSortBooks(
                owner.id, 0, 10, FilterCriteria.AVAILABLE, SortCriteria.DATE_DESC
            )
            availableBooks.total shouldBe 2
            val availableTitles = availableBooks.items.map { it.book.title }
            availableTitles shouldContainAll listOf("Rayuela", "El Aleph")
        }
    }
})