package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthResponse
import org.springframework.data.repository.CrudRepository
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

interface CrudUserRepository: CrudRepository<User, Long>{

    fun findByEmail(email: String): Optional<User>

}