package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.UserDTO
import ar.edu.unsam.phm.dto.toUserDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {

    fun getUserProfile(userId: Int): UserDTO {
        val user = userRepository.repositoryObjects().find { user -> user.id == userId }
        if (user == null) {
            throw NotFoundException("No se encontro un user con el id: $userId")
        }
        return user.toUserDTO()
    }

}