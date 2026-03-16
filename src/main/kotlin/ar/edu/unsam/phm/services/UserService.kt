package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException

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

}