package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.domain.Collectable
import ar.edu.unsam.phm.domain.Common
import ar.edu.unsam.phm.domain.WithADedication
import ar.edu.unsam.phm.repository.MongoBookRepository
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CalificationAnalisisDataFetcherTest {

    @Autowired lateinit var dgsQueryExecutor: DgsQueryExecutor
    @Autowired lateinit var bookRepository: MongoBookRepository

    private val QUERY = "{ calificactionAnalisis { bookType avgRating } }"

    @BeforeEach
    fun setup() {
        bookRepository.deleteAll()
    }

    @Test
    fun `devuelve lista vacía cuando ningún libro tiene reseñas`() {
        bookRepository.save(Common().apply { ratingAvg = 0.0 })

        val result = dgsQueryExecutor.executeAndExtractJsonPath<List<Any>>(
            QUERY, "$.data.calificactionAnalisis"
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `excluye libros eliminados del cálculo`() {
        bookRepository.save(Common().apply {
            ratingAvg = 4.0
            deleted = true
        })

        val result = dgsQueryExecutor.executeAndExtractJsonPath<List<Any>>(
            QUERY, "$.data.calificactionAnalisis"
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `calcula el promedio de ratingAvg correctamente por tipo`() {
        bookRepository.save(Common().apply { ratingAvg = 4.0 })
        bookRepository.save(Common().apply { ratingAvg = 2.0 })

        val avgRating = dgsQueryExecutor.executeAndExtractJsonPath<Double>(
            QUERY, "$.data.calificactionAnalisis[0].avgRating"
        )

        assertThat(avgRating).isEqualTo(3.0)
    }

    @Test
    fun `devuelve solo los tipos que tienen libros con reseñas`() {
        bookRepository.save(Common().apply { ratingAvg = 4.0 })
        bookRepository.save(WithADedication().apply { ratingAvg = 0.0 })
        bookRepository.save(Collectable().apply { ratingAvg = 0.0 })

        val bookTypes = dgsQueryExecutor.executeAndExtractJsonPath<List<String>>(
            QUERY, "$.data.calificactionAnalisis[*].bookType"
        )

        assertThat(bookTypes).containsExactly("COMUN")
    }

    @Test
    fun `redondea el promedio a dos decimales`() {
        // promedio = (4.0 + 3.0 + 2.5) / 3 = 3.1666... → 3.17
        bookRepository.save(Common().apply { ratingAvg = 4.0 })
        bookRepository.save(Common().apply { ratingAvg = 3.0 })
        bookRepository.save(Common().apply { ratingAvg = 2.5 })

        val avgRating = dgsQueryExecutor.executeAndExtractJsonPath<Double>(
            QUERY, "$.data.calificactionAnalisis[0].avgRating"
        )

        assertThat(avgRating).isEqualTo(3.17)
    }

    @Test
    fun `agrupa correctamente libros de distintos tipos`() {
        bookRepository.save(Common().apply { ratingAvg = 4.0 })
        bookRepository.save(WithADedication().apply { ratingAvg = 3.0 })
        bookRepository.save(Collectable().apply { ratingAvg = 5.0 })

        val bookTypes = dgsQueryExecutor.executeAndExtractJsonPath<List<String>>(
            QUERY, "$.data.calificactionAnalisis[*].bookType"
        )

        assertThat(bookTypes).containsExactlyInAnyOrder("COMUN", "CON DEDICATORIA", "COLECCIONABLE")
    }
}