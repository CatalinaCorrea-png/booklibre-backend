package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.toOwnerDTO
import ar.edu.unsam.phm.repository.*
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BookDetailControllerIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var bookRepository: MongoBookRepository
    @Autowired lateinit var userRepository: CrudUserRepository
    @Autowired lateinit var authorRepository: CrudAuthorRepository
    @Autowired lateinit var reservationRepository: CrudReservationRepository

    lateinit var book: Book

    @BeforeEach
    fun setup() {
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()
        authorRepository.deleteAll()

        val owner = userRepository.save(User(
            name = "Tolkien",
            email = "tolkien@test.com",
            userType = UserTypes.PUBLISHER
        ))
        val author = authorRepository.save(Author("J.R.R. Tolkien", "avatar.jpg"))
        book = bookRepository.save(Common().apply {
            title = "El Señor de los Anillos"
            desc = "Épica de fantasía"
            gender = Gender.DRAMA
            this.author = author
            numPages = 1178
            isbn = "978-0-618-00224-4"
            language = Language.SPANISH
            editorial = "Minotauro"
            publishDate = LocalDate.of(1954, 7, 29)
            condition = BookCondition.EXCELLENT
            this.owner = owner.toOwnerDTO()
            imageSrc = "lotr.jpg"
        })
    }

    @Test
    fun `Caso feliz - retorna el libro cuando existe`() {
        mockMvc.perform(get("/book-detail/${book.id}").param("userId", "some-user-id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("El Señor de los Anillos"))
            .andExpect(jsonPath("$.id").value(book.id))
    }

    @Test
    fun `Caso triste - retorna 404 cuando el libro no existe`() {
        mockMvc.perform(get("/book-detail/id-inexistente").param("userId", "some-user-id"))
            .andExpect(status().isNotFound)
    }
}