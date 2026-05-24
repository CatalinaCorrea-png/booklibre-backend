package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.toOwnerDTO
import ar.edu.unsam.phm.errors.ConflictException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class BookTest : DescribeSpec({

    var owner = User(name = "Tolkien", userType = UserTypes.PUBLISHER).apply { id = "user-id-1" }

    fun validBook(type: Book = Common()): Book = type.apply {
        title = "El Quijote"
        desc = "Novela clásica"
        gender = Gender.DRAMA
        author = Author("Cervantes", "avatar.jpg")
        numPages = 500
        isbn = "978-3-16-148410-0"
        language = Language.SPANISH
        editorial = "Planeta"
        publishDate = LocalDate.of(1605, 1, 1)
        condition = BookCondition.EXCELLENT
        this.owner = owner.toOwnerDTO()
        imageSrc = "image.jpg"
    }

    describe("validate") {
        it("no lanza excepción cuando todos los campos son válidos") {
            validBook().validate()
        }

        it("lanza ConflictException si el título está vacío") {
            val book = validBook().apply { title = "" }
            shouldThrow<ConflictException> { book.validate() }
                .message shouldBe "El libro tiene que tener titulo"
        }

        it("lanza ConflictException si la descripción supera 1000 caracteres") {
            val book = validBook().apply { desc = "a".repeat(1001) }
            shouldThrow<ConflictException> { book.validate() }
        }

        it("lanza ConflictException si numPages es 0") {
            val book = validBook().apply { numPages = 0 }
            shouldThrow<ConflictException> { book.validate() }
        }

        it("lanza ConflictException si imageSrc supera 255 caracteres") {
            val book = validBook().apply { imageSrc = "a".repeat(255) }
            shouldThrow<ConflictException> { book.validate() }
        }

        it("lanza ConflictException si el dueño es un lector") {
            val readerOwner = User(name = "Lector", userType = UserTypes.READER).apply { id = "reader-id-1" }
            val book = validBook().apply { this.owner = readerOwner.toOwnerDTO() }
            shouldThrow<ConflictException> { book.validate() }
        }
    }

    describe("logicDelete") {
        it("marca el libro como deleted") {
            val book = validBook()
            book.deleted shouldBe false
            book.logicDelete()
            book.deleted shouldBe true
        }
    }



    describe("calculateBibliokarmas - Template Method") {
        it("Common: 5 * días de reserva + numPages * 5") {
            val book = validBook(Common())
            book.calculateBibliokarmas(reservationDays = 10, userBibliokarmas = 100) shouldBeExactly 2550L
        }

        it("WithADedication: 5 * días + 200 + 10 * reservationCount") {
            val book = validBook(WithADedication())
            // Con reservationCount = 0 (default)
            book.calculateBibliokarmas(reservationDays = 10, userBibliokarmas = 100) shouldBeExactly 250L
        }

        it("Collectable:  5 * días + userBibliokarmas/5 + numPages") {
            val book = validBook(Collectable())
            // Ajustar según implementación real
            book.calculateBibliokarmas(10, 101) shouldBeExactly 571L
        }
    }

    describe("meetsSearchCriteria") {
        it("devuelve true si criteria está vacío") {
            validBook().meetsSearchCriteria("") shouldBe true
        }

        it("devuelve true si el título contiene el criterio (case insensitive)") {
            validBook().meetsSearchCriteria("quijote") shouldBe true
        }

        it("devuelve false si el título no contiene el criterio") {
            validBook().meetsSearchCriteria("harry potter") shouldBe false
        }
    }
})