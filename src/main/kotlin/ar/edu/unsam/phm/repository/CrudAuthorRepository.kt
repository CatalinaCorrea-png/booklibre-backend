package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Author
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface CrudAuthorRepository: CrudRepository<Author, Int> {
    fun findByName(name: String): Optional<Author>
}