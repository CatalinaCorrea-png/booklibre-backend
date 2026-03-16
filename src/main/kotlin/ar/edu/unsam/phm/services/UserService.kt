package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {
    fun getUserById(id: Int): User =
        userRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")

}