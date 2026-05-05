package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.config.JwtProperties
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.services.TokenService
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("sectest")
@Transactional
class JwtSecurityIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var userRepository: CrudUserRepository
    @Autowired lateinit var tokenService: TokenService
    @Autowired lateinit var jwtProperties: JwtProperties
    @Autowired lateinit var passwordEncoder: PasswordEncoder
    @Autowired lateinit var entityManager: EntityManager

    lateinit var publisher: User
    lateinit var reader: User
    lateinit var combined: User

    lateinit var publisherToken: String
    lateinit var readerToken: String
    lateinit var combinedToken: String

    private val rawPassword = "password123"

    @BeforeEach
    fun setup() {
        publisher = userRepository.save(User(
            name = "Publisher Test",
            email = "publisher@sectest.com",
            password = passwordEncoder.encode(rawPassword),
            userType = UserTypes.PUBLISHER,
            bibliokarmas = 100
        ))
        reader = userRepository.save(User(
            name = "Reader Test",
            email = "reader@sectest.com",
            password = passwordEncoder.encode(rawPassword),
            userType = UserTypes.READER,
            bibliokarmas = 100
        ))
        combined = userRepository.save(User(
            name = "Combined Test",
            email = "combined@sectest.com",
            password = passwordEncoder.encode(rawPassword),
            userType = UserTypes.COMBINED,
            bibliokarmas = 100
        ))
        entityManager.flush()

        publisherToken = generateToken(publisher)
        readerToken = generateToken(reader)
        combinedToken = generateToken(combined)
    }

    private fun generateToken(user: User): String {
        val userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(user.userType.name)
            .build()
        return tokenService.generate(
            userDetails = userDetails,
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        )
    }

    private fun generateExpiredToken(user: User): String {
        val userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(user.userType.name)
            .build()
        return tokenService.generate(
            userDetails = userDetails,
            expirationDate = Date(System.currentTimeMillis() - 1000)
        )
    }

    private fun assertSecurityPassed(status: Int, context: String) {
        assert(status != 401 && status != 403) {
            "Esperaba acceso permitido para $context, pero recibió $status"
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Sin autenticación
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Sin token")
    inner class SinToken {

        @Test
        fun `endpoint protegido sin token devuelve 401`() {
            mockMvc.perform(get("/filtered-books").param("userId", "1"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `login es accesible sin token`() {
            mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"publisher@sectest.com","password":"$rawPassword"}"""))
                .andExpect(status().isOk)
        }

        @Test
        fun `register es accesible sin token`() {
            mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Nuevo","email":"nuevo@sectest.com","password":"12345678"}"""))
                .andExpect { assertSecurityPassed(it.response.status, "register") }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Token expirado
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Token expirado")
    inner class TokenExpirado {

        @Test
        fun `token expirado devuelve 401`() {
            val expired = generateExpiredToken(publisher)
            mockMvc.perform(get("/filtered-books")
                .header("Authorization", "Bearer $expired")
                .param("userId", "1"))
                .andExpect(status().isUnauthorized)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Rol PUBLISHER
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUBLISHER")
    inner class PublisherTests {

        @Test
        fun `puede crear libro`() {
            mockMvc.perform(post("/create-book")
                .header("Authorization", "Bearer $publisherToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect { assertSecurityPassed(it.response.status, "PUBLISHER crear libro") }
        }

        @Test
        fun `puede editar libro`() {
            mockMvc.perform(put("/edit-book/999")
                .header("Authorization", "Bearer $publisherToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect { assertSecurityPassed(it.response.status, "PUBLISHER editar libro") }
        }

        @Test
        fun `puede eliminar libro`() {
            mockMvc.perform(delete("/delete-book/999")
                .header("Authorization", "Bearer $publisherToken"))
                .andExpect { assertSecurityPassed(it.response.status, "PUBLISHER eliminar libro") }
        }

        @Test
        fun `puede ver sus libros`() {
            mockMvc.perform(get("/userOwnBooks/${publisher.id}")
                .header("Authorization", "Bearer $publisherToken"))
                .andExpect { assertSecurityPassed(it.response.status, "PUBLISHER ver sus libros") }
        }

        @Test
        fun `no puede crear reserva`() {
            mockMvc.perform(post("/create-reservation")
                .header("Authorization", "Bearer $publisherToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `no puede calificar`() {
            mockMvc.perform(post("/1/calificar")
                .header("Authorization", "Bearer $publisherToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":5,"review":"Genial"}""")
                .param("userId", publisher.id.toString()))
                .andExpect(status().isForbidden)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Rol READER
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("READER")
    inner class ReaderTests {

        @Test
        fun `puede crear reserva`() {
            mockMvc.perform(post("/create-reservation")
                .header("Authorization", "Bearer $readerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect { assertSecurityPassed(it.response.status, "READER crear reserva") }
        }

        @Test
        fun `puede calificar`() {
            mockMvc.perform(post("/1/calificar")
                .header("Authorization", "Bearer $readerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":5,"review":"Genial"}""")
                .param("userId", reader.id.toString()))
                .andExpect { assertSecurityPassed(it.response.status, "READER calificar") }
        }

        @Test
        fun `no puede crear libro`() {
            mockMvc.perform(post("/create-book")
                .header("Authorization", "Bearer $readerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `no puede editar libro`() {
            mockMvc.perform(put("/edit-book/999")
                .header("Authorization", "Bearer $readerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `no puede eliminar libro`() {
            mockMvc.perform(delete("/delete-book/999")
                .header("Authorization", "Bearer $readerToken"))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `no puede ver libros de publisher`() {
            mockMvc.perform(get("/userOwnBooks/${publisher.id}")
                .header("Authorization", "Bearer $readerToken"))
                .andExpect(status().isForbidden)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Rol COMBINED
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("COMBINED")
    inner class CombinedTests {

        @Test
        fun `puede crear libro`() {
            mockMvc.perform(post("/create-book")
                .header("Authorization", "Bearer $combinedToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect { assertSecurityPassed(it.response.status, "COMBINED crear libro") }
        }

        @Test
        fun `puede crear reserva`() {
            mockMvc.perform(post("/create-reservation")
                .header("Authorization", "Bearer $combinedToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect { assertSecurityPassed(it.response.status, "COMBINED crear reserva") }
        }

        @Test
        fun `puede calificar`() {
            mockMvc.perform(post("/1/calificar")
                .header("Authorization", "Bearer $combinedToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":5,"review":"Genial"}""")
                .param("userId", combined.id.toString()))
                .andExpect { assertSecurityPassed(it.response.status, "COMBINED calificar") }
        }

        @Test
        fun `puede ver sus libros`() {
            mockMvc.perform(get("/userOwnBooks/${combined.id}")
                .header("Authorization", "Bearer $combinedToken"))
                .andExpect { assertSecurityPassed(it.response.status, "COMBINED ver sus libros") }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Refresh token
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Refresh token")
    inner class RefreshTokenTests {

        @Test
        fun `refresh sin cookie devuelve 401`() {
            mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `refresh con cookie válida devuelve nuevo access token`() {
            val authResult = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"publisher@sectest.com","password":"$rawPassword"}"""))
                .andExpect(status().isOk)
                .andReturn()

            val refreshCookie = authResult.response.getCookie("refreshToken")
                ?: throw AssertionError("No se recibió cookie refreshToken")

            mockMvc.perform(post("/api/auth/refresh")
                .cookie(refreshCookie))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").isString)
        }
    }
}
