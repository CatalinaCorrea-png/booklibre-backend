import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.repository.CrudAuthorRepository
import ar.edu.unsam.phm.repository.CrudBookRepository
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.services.BookService
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

    // --- Mocks (replaces in-memory repos — BookService requires JPA interfaces) ---
    val bookRepository = mockk<CrudBookRepository>()
    val reservationRepository = mockk<CrudReservationRepository>()
    val userRepository = mockk<CrudUserRepository>()
    val authorRepository = mockk<CrudAuthorRepository>()
    val bookService = BookService(bookRepository, reservationRepository, userRepository, authorRepository)

    val userId = 1L

    // --- Sample DTOs (flat, no longer wrapped in Reservation) ---
    val bookADTO = ProfileBookDTO(
        id = 1L, title = "1984", author = "George Orwell",
        gender = Gender.DRAMA, timestamp = LocalDate.of(2024, 1, 1),
        imageSrc = "", state = "PRESTADO"
    )
    val bookBDTO = ProfileBookDTO(
        id = 2L, title = "Rayuela", author = "Julio Cortázar",
        gender = Gender.DRAMA, timestamp = LocalDate.of(2024, 2, 1),
        imageSrc = "", state = "DISPONIBLE"
    )
    val bookCDTO = ProfileBookDTO(
        id = 3L, title = "El Aleph", author = "Jorge Luis Borges",
        gender = Gender.DRAMA, timestamp = LocalDate.of(2024, 3, 1),
        imageSrc = "", state = "DISPONIBLE"
    )

    describe("getAllUserBooks devuelve todos los libros del usuario") {

        it("Cada libro aparece exactamente una vez con su estado actual") {
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))
            every {
                bookRepository.getAllUserBooks(userId, any(), FilterCriteria.ALL.name)
            } returns PageImpl(listOf(bookADTO, bookBDTO, bookCDTO), pageable, 3)

            val result = bookService.getAllUserBooks(
                userId,
                ProfileBookPageable(FilterCriteria.ALL, SortCriteria.DATE_DESC, 0, 10)
            )

            result.items shouldHaveSize 3
            result.items.map { it.id } shouldContainAll listOf(1L, 2L, 3L)
        }

        it("Un libro con reserva activa aparece como PRESTADO") {
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))
            every {
                bookRepository.getAllUserBooks(userId, any(), FilterCriteria.ALL.name)
            } returns PageImpl(listOf(bookADTO), pageable, 1)

            val result = bookService.getAllUserBooks(
                userId,
                ProfileBookPageable(FilterCriteria.ALL, SortCriteria.DATE_DESC, 0, 10)
            )

            result.items.first().state shouldBe "PRESTADO"
        }
    }

    describe("El filtro de perfil clasifica correctamente DISPONIBLE vs PRESTADO") {

        it("BORROWED muestra solo libros actualmente prestados, AVAILABLE los disponibles, ALL todos") {
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))

            every {
                bookRepository.getAllUserBooks(userId, any(), FilterCriteria.ALL.name)
            } returns PageImpl(listOf(bookADTO, bookBDTO, bookCDTO), pageable, 3)

            every {
                bookRepository.getAllUserBooks(userId, any(), FilterCriteria.BORROWED.name)
            } returns PageImpl(listOf(bookADTO), pageable, 1)

            every {
                bookRepository.getAllUserBooks(userId, any(), FilterCriteria.AVAILABLE.name)
            } returns PageImpl(listOf(bookBDTO, bookCDTO), pageable, 2)

            // ALL → 3 libros
            val allBooks = bookService.getAllUserBooks(
                userId, ProfileBookPageable(FilterCriteria.ALL, SortCriteria.DATE_DESC, 0, 10)
            )
            allBooks.items shouldHaveSize 3

            // BORROWED → solo bookA (PRESTADO)
            val borrowedBooks = bookService.getAllUserBooks(
                userId, ProfileBookPageable(FilterCriteria.BORROWED, SortCriteria.DATE_DESC, 0, 10)
            )
            borrowedBooks.items shouldHaveSize 1
            borrowedBooks.items.first().title shouldBe "1984"
            borrowedBooks.items.first().state shouldBe "PRESTADO"

            // AVAILABLE → bookB y bookC (DISPONIBLE)
            val availableBooks = bookService.getAllUserBooks(
                userId, ProfileBookPageable(FilterCriteria.AVAILABLE, SortCriteria.DATE_DESC, 0, 10)
            )
            availableBooks.items shouldHaveSize 2
            availableBooks.items.map { it.title } shouldContainAll listOf("Rayuela", "El Aleph")
        }
    }
})
