package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.toOwnerDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.util.Optional

class BookDetailSpec : DescribeSpec({
    isolationMode = IsolationMode.InstancePerTest

    val bookRepository            = mockk<MongoBookRepository>()
    val reservationRepository     = mockk<CrudReservationRepository>(relaxed = true)
    val userRepository            = mockk<CrudUserRepository>(relaxed = true)
    val authorRepository          = mockk<CrudAuthorRepository>(relaxed = true)
    val reviewRepository          = mockk<CrudReviewRepository>(relaxed = true)
    val bookCacheService          = mockk<BookCacheService>(relaxed = true)

    val bookService = BookService(
        bookRepository, reservationRepository, userRepository, authorRepository, reviewRepository, bookCacheService
    )

    val owner = User(name = "Tolkien", userType = UserTypes.PUBLISHER).apply { id = "user-id-1" }
    val book = Common().apply {
        id = "book-id-1"
        title = "El Señor de los Anillos"
        desc = "Épica de fantasía"
        gender = Gender.DRAMA
        author = Author("J.R.R. Tolkien", "avatar.jpg")
        numPages = 1178
        isbn = "978-0-618-00224-4"
        language = Language.SPANISH
        editorial = "Minotauro"
        publishDate = LocalDate.of(1954, 7, 29)
        condition = BookCondition.EXCELLENT
        this.owner = owner.toOwnerDTO()
        imageSrc = "lotr.jpg"
    }

    describe("getBookById") {
        it("Caso feliz: devuelve el BookDTO cuando el libro existe") {
            every { bookRepository.findById("book-id-1") } returns Optional.of(book)

            val result = bookService.getBookById("book-id-1")

            result.title shouldBe "El Señor de los Anillos"
            result.id shouldBe "book-id-1"
        }

        it("Caso triste: lanza NotFoundException cuando el libro no existe") {
            every { bookRepository.findById("id-inexistente") } returns Optional.empty()

            shouldThrow<NotFoundException> { bookService.getBookById("id-inexistente") }
        }
    }
})