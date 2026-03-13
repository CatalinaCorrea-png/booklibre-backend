package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {
}