package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.repository.*
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc   // sin addFilters = false → los filtros de seguridad están activos
@ActiveProfiles("sectest")
@Transactional
class BookDetailSecurityTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var bookRepository: CrudBookRepository
    @Autowired lateinit var userRepository: CrudUserRepository
    @Autowired lateinit var authorRepository: CrudAuthorRepository
    @Autowired lateinit var reservationRepository: CrudReservationRepository
    @Autowired lateinit var entityManager: EntityManager

    lateinit var book: Book

    @BeforeEach
    fun setup() {
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()
        authorRepository.deleteAll()

        val owner = userRepository.save(User(
            name = "Tolkien",
            email = "tolkien.sec@test.com",
            userType = UserTypes.PUBLISHER
        ))
        val author = authorRepository.save(Author("J.R.R. Tolkien", "avatar.jpg"))
        book = bookRepository.save(Common().apply {
            title = "El Señor de los Anillos"
            desc = "Épica de fantasía"
            gender = Gender.DRAMA
            this.author = author
            numPages = 1178
            isbn = "978-0-618-00224-5"
            language = Language.SPANISH
            editorial = "Minotauro"
            publishDate = LocalDate.of(1954, 7, 29)
            condition = BookCondition.EXCELLENT
            this.owner = owner
            imageSrc = "lotr.jpg"
        })
        entityManager.flush()
    }

    @Test
    @WithMockUser(authorities = ["READER"])
    fun `Caso feliz - un LECTOR puede acceder al detalle del libro`() {
        mockMvc.perform(get("/book-detail/${book.id}"))
            .andExpect(status().isOk)
    }
}