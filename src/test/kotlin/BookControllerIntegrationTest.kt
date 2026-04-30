package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.repository.*
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
//@org.springframework.test.context.junit.jupiter.SpringJUnitConfig
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class BookControllerIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var bookRepository: CrudBookRepository
    @Autowired lateinit var userRepository: CrudUserRepository
    @Autowired lateinit var authorRepository: CrudAuthorRepository
    @Autowired lateinit var reservationRepository: CrudReservationRepository
    @Autowired lateinit var entityManager: EntityManager

    lateinit var owner: User
    lateinit var otherUser: User
    lateinit var author: Author
    lateinit var book1: Book
    lateinit var book2: Book
    lateinit var book3: Book

    @BeforeEach
    fun setup() {
        reservationRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()
        authorRepository.deleteAll()

        owner = userRepository.save(User(
            name = "Juan Pérez",
            email = "juan@test.com",
            bibliokarmas = 100
        ))

        otherUser = userRepository.save(User(
            name = "María López",
            email = "maria@test.com",
            bibliokarmas = 50
        ))

        author = authorRepository.save(Author("García Márquez", "avatar.jpg"))

        book1 = bookRepository.save(Common().apply {
            title = "Cien años de soledad"
            desc = "Novela"
            gender = Gender.DRAMA
            author = this@BookControllerIntegrationTest.author
            numPages = 400
            isbn = "978-0-00-000001-1"
            owner = this@BookControllerIntegrationTest.owner
            imageSrc = "img1.jpg"
            editorial = "Sudamericana"
        })
        book2 = bookRepository.save(Common().apply {
            title = "El amor en los tiempos del cólera"
            desc = "Romance"
            gender = Gender.ROMANCE
            author = this@BookControllerIntegrationTest.author
            numPages = 200
            isbn = "978-0-00-000002-2"
            owner = this@BookControllerIntegrationTest.owner
            imageSrc = "img2.jpg"
            editorial = "Sudamericana"
        })
        book3 = bookRepository.save(Common().apply {
            title = "Otro libro"
            desc = "Descripción"
            gender = Gender.DRAMA
            author = this@BookControllerIntegrationTest.author
            numPages = 600
            isbn = "978-0-00-000003-3"
            owner = this@BookControllerIntegrationTest.otherUser  // de otro owner
            imageSrc = "img3.jpg"
            editorial = "Otra"
        })
        entityManager.flush()
    }

    @Test
    fun `devuelve solo libros que no son del usuario consultante`() {
        mockMvc.perform(get("/filtered-books").param("userId", otherUser.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].title", org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.hasItem("Otro libro")
            )))
    }

    @Test
    fun `filtra por título (case insensitive, LIKE)`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("title", "soledad"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Cien años de soledad"))
    }

    @Test
    fun `filtra por rango de páginas`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("pagesRangeMin", "300")
            .param("pagesRangeMax", "500"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Cien años de soledad"))
    }

    @Test
    fun `filtra por género`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("genders", "ROMANCE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].gender").value("Romance"))
    }

    @Test
    fun `filtra por ISBN parcial`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("isbn", "000001"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `filtra por nombre del owner`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("ownersName", "juan"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `excluye libros con reservas que se solapan con el rango de fechas`() {
        // Reserva book1 del 10 al 20
        reservationRepository.save(Reservation(
            book = book1,
            user = otherUser,
            pickUpDate = LocalDate.of(2026, 5, 10),
            dropOffDate = LocalDate.of(2026, 5, 20)
        ))

        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("pickUpDate", "2026-05-15")
            .param("dropOffDate", "2026-05-18"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("El amor en los tiempos del cólera"))
    }

    @Test
    fun `incluye libros si las fechas no se solapan con la reserva`() {
        reservationRepository.save(Reservation(
            book = book1,
            user = otherUser,
            pickUpDate = LocalDate.of(2026, 5, 10),
            dropOffDate = LocalDate.of(2026, 5, 20)
        ))

        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("pickUpDate", "2026-06-01")
            .param("dropOffDate", "2026-06-05"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `excluye libros eliminados logicamente`() {
        book1.logicDelete()
        bookRepository.save(book1)

        mockMvc.perform(get("/filtered-books").param("userId", otherUser.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `pagina correctamente`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("page", "0")
            .param("pageSize", "1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
    }

    @Test
    fun `ordena por título ascendente por default`() {
        mockMvc.perform(get("/filtered-books").param("userId", otherUser.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].title").value("Cien años de soledad"))
            .andExpect(jsonPath("$.content[1].title").value("El amor en los tiempos del cólera"))
    }

    @Test
    fun `ordena descendente cuando ascending es false`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("ascending", "false"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].title").value("El amor en los tiempos del cólera"))
    }

    @Test
    fun `calcula bibliokarmas correctamente en el DTO`() {
        mockMvc.perform(get("/filtered-books")
            .param("userId", otherUser.id.toString())
            .param("pickUpDate", "2026-05-10")
            .param("dropOffDate", "2026-05-15"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].bookBibliokarmas").isNumber)
    }
}