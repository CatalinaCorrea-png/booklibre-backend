import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.repository.*
import ar.edu.unsam.phm.services.BookService
import ar.edu.unsam.phm.services.BookCacheService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldContainAll
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

class ProfileSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val bookRepository = mockk<MongoBookRepository>()
    val reservationRepository = mockk<CrudReservationRepository>()
    val userRepository = mockk<CrudUserRepository>()
    val authorRepository = mockk<CrudAuthorRepository>()
    val reviewRepository = mockk<CrudReviewRepository>(relaxed = true)
    val bookCacheService = mockk<BookCacheService>(relaxed = true)
    val bookService = BookService(bookRepository, reservationRepository, userRepository, authorRepository, reviewRepository, bookCacheService)

    val userId = "user-id-1"

    val owner = User(name = "Tolkien", userType = UserTypes.PUBLISHER).apply { id = userId }

    val bookA = Common().apply {
        id = "1"
        title = "1984"
        this.author = Author("George Orwell", "avatar.jpg")
        gender = Gender.DRAMA
        createdAt = LocalDate.of(2024, 1, 1)
        imageSrc = ""
        this.owner = owner.toOwnerDTO()
        desc = "Descripcion"
        numPages = 300
        isbn = "978-0-00-000001-1"
        editorial = "Editorial"
        // reserva activa → PRESTADO
        reservations = mutableListOf(ReservationDatesDTO(
            resevationId = "res-1",
            pickUpDate = LocalDate.now().minusDays(5),
            dropOffDate = LocalDate.now().plusDays(5)
        ))
    }

    val bookB = Common().apply {
        id = "2"
        title = "Rayuela"
        this.author = Author("Julio Cortázar", "avatar.jpg")
        gender = Gender.DRAMA
        createdAt = LocalDate.of(2024, 2, 1)
        imageSrc = ""
        this.owner = owner.toOwnerDTO()
        desc = "Descripcion"
        numPages = 300
        isbn = "978-0-00-000002-2"
        editorial = "Editorial"
    }

    val bookC = Common().apply {
        id = "3"
        title = "El Aleph"
        this.author = Author("Jorge Luis Borges", "avatar.jpg")
        gender = Gender.DRAMA
        createdAt = LocalDate.of(2024, 3, 1)
        imageSrc = ""
        this.owner = owner.toOwnerDTO()
        desc = "Descripcion"
        numPages = 300
        isbn = "978-0-00-000003-3"
        editorial = "Editorial"
    }

    describe("getAllUserBooks devuelve todos los libros del usuario") {

        it("Cada libro aparece exactamente una vez con su estado actual") {
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
            every {
                bookRepository.findUserBooks(userId, any(), any())
            } returns PageImpl(listOf(bookA, bookB, bookC), pageable, 3)

            val result = bookService.getAllUserBooks(
                userId,
                ProfileBookPageable(FilterCriteria.ALL, SortCriteria.DATE_DESC, 0, 10)
            )

            result.items shouldHaveSize 3
            result.items.map { it.id } shouldContainAll listOf("1", "2", "3")
        }

        it("Un libro con reserva activa aparece como PRESTADO") {
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
            every {
                bookRepository.findUserBooks(userId, any(), any())
            } returns PageImpl(listOf(bookA), pageable, 1)

            val result = bookService.getAllUserBooks(
                userId,
                ProfileBookPageable(FilterCriteria.ALL, SortCriteria.DATE_DESC, 0, 10)
            )

            result.items.first().state shouldBe BookAvailability.BORROWED
        }
    }

    describe("El filtro de perfil clasifica correctamente DISPONIBLE vs PRESTADO") {

        it("BORROWED muestra solo libros actualmente prestados, AVAILABLE los disponibles, ALL todos") {
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))

            every {
                bookRepository.findUserBooks(userId, any(), any())
            } returns PageImpl(listOf(bookA, bookB, bookC), pageable, 3)

            val allBooks = bookService.getAllUserBooks(
                userId, ProfileBookPageable(FilterCriteria.ALL, SortCriteria.DATE_DESC, 0, 10)
            )
            allBooks.items shouldHaveSize 3

            every {
                bookRepository.findUserBooks(userId, any(), any())
            } returns PageImpl(listOf(bookA), pageable, 1)

            val borrowedBooks = bookService.getAllUserBooks(
                userId, ProfileBookPageable(FilterCriteria.BORROWED, SortCriteria.DATE_DESC, 0, 10)
            )
            borrowedBooks.items shouldHaveSize 1
            borrowedBooks.items.first().title shouldBe "1984"
            borrowedBooks.items.first().state shouldBe BookAvailability.BORROWED

            every {
                bookRepository.findUserBooks(userId, any(), any())
            } returns PageImpl(listOf(bookB, bookC), pageable, 2)

            val availableBooks = bookService.getAllUserBooks(
                userId, ProfileBookPageable(FilterCriteria.AVAILABLE, SortCriteria.DATE_DESC, 0, 10)
            )
            availableBooks.items shouldHaveSize 2
            availableBooks.items.map { it.title } shouldContainAll listOf("Rayuela", "El Aleph")
        }
    }
})