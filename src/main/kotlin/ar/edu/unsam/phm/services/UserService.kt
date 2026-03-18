package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.UserDTO
import ar.edu.unsam.phm.dto.toUserDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service

import ar.edu.unsam.phm.errors.BusinessException


@Service
class UserService(
    val userRepository: UserRepository
) {
    fun validate(user: User): User {
        val userRepo = this.search(user)
        if (userRepo.password == user.password) {
            return userRepo
        }else {
            throw BusinessException("Las credenciales no coinciden")
        }
    }

    fun search( user: User) : User {
        val userMatch = userRepository.search(user.email)
        if (userMatch.isEmpty()){
            throw NotFoundException("Credenciales incorrectas")
        }else{
            return userMatch.first()
        }
    }

    fun create(user: User) {
        val existingUser: List<User> = userRepository.search(user.email)
        if (existingUser.isEmpty()){
            user.meetsCreationCriteria()
            userRepository.create(user)
        }else{
            throw BusinessException("Email incorrecto")
        }
    }

    fun getUserById(id: Int): User =
        userRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")


    fun getUserProfile(userId: Int): UserDTO {
        val user = userRepository.repositoryObjects().find { user -> user.id == userId }
        if (user == null) {
            throw NotFoundException("No se encontro un user con el id: $userId")
        }
        return user.toUserDTO()
    }

}