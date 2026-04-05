package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.dto.UpdateUserProfileDTO
import ar.edu.unsam.phm.errors.NotFoundException
import org.springframework.stereotype.Service
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.repository.CrudUserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


@Service
class UserService(

    @Autowired
    val userRepository: CrudUserRepository
) {

    @Transactional(readOnly = true)
    fun getUser(user: User): User {
        val persistedUser = userRepository
            .findByEmail(user.email)
            .orElseThrow {
                NotFoundException("No se encuentra un usuario registrado con ese mail")
            }

        if (persistedUser.password != user.password) throw BusinessException("Las credenciales no coinciden")

        return persistedUser
    }

    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): User {
        val persistedUser = userRepository
            .findById(userId)
            .orElseThrow {
                NotFoundException("No se encuentra un usuario registrado con este ID: $userId")
            }
        return persistedUser
    }

//
//    fun search( user: User) : User {
//        val userMatch = userRepository.findByEmail(user.email)
//        if (userMatch.isEmpty){
//            throw BusinessException("Credenciales incorrectas")
//        }else{
//            return userMatch
//        }
//    }
    @Transactional
    fun create(user: User): User {
        val existingUser: Optional<User> = userRepository.findByEmail(user.email)
        if (existingUser.isEmpty) {
            user.validate()
            return userRepository.save(user)
        }  else {
            throw ConflictException("Email '${user.email}' ya se encuentra registrado")
    }}

    fun updateUserProfile(userData: UpdateUserProfileDTO): User {
        val existingUser = userRepository
            .findById(userData.id)
            .orElseThrow {
                NotFoundException("No se encuentra un usuario registrado con ese ID ${userData.id}")
            }

        val updatedUser = User(
            name = userData.name,
            description = userData.description,
            email = userData.email,
            cel = userData.cel,
            location = userData.location,
            userType = UserTypes.fromValue(userData.userType),
            timestamp = userData.timestamp,
            bibliokarmas = userData.bibliokarmas,
            password = existingUser.password,
            img = userData.img
        ).apply {
            id = existingUser.id
        }

        userRepository.save(updatedUser)

        return updatedUser
    }

}