package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.User
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface CrudUserRepository: CrudRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>

}