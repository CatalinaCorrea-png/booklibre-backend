package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.State
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.dto.UpdateUserProfileDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(
    @Autowired
    val userRepository: CrudUserRepository,
    @Autowired
    val reservationRepository: CrudReservationRepository,
    private val encoder: PasswordEncoder
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

    @Transactional
    fun create(user: User): User {
        val existingUser: Optional<User> = userRepository.findByEmail(user.email)
        if (existingUser.isEmpty) {
            val userCopy = User(
                name = user.name,
                email = user.email,
                password = encoder.encode(user.password)
            )
            userCopy.validate()
            return userRepository.save(userCopy)
        } else {
            throw ConflictException("Email '${user.email}' ya se encuentra registrado")
        }
    }

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

        if (existingUser.userType.name != updatedUser.userType.name) {
            if (updatedUser.userType.name == UserTypes.READER.name) {
                validateActiveReservationsAsPublisher(existingUser.id!!)
            }
            if (updatedUser.userType.name == UserTypes.PUBLISHER.name) {
                validateActiveReservationsAsReader(existingUser.id!!)
            }
        }

        userRepository.save(updatedUser)

        return updatedUser
    }

    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User {
        val persistedUser = userRepository
            .findByEmail(email)
            .orElseThrow {
                NotFoundException("No se encuentra un usuario registrado con este email: $email")
            }
        return persistedUser
    }

    fun validateActiveReservationsAsPublisher(userId: Long) {
        var reservations = reservationRepository.findAllByBook_Owner_Id(userId)
        if(reservations.any { it.dropOffDate >= LocalDate.now() }) {
            throw BusinessException("Tenes reservas activas. No podes cambiar tu tipo a lector. Respetá")
        }
    }

    fun validateActiveReservationsAsReader(userId: Long) {
            var reservations = reservationRepository.findAllByUser_Id(userId)
            if(reservations.any { it.dropOffDate >= LocalDate.now() }) {
                throw BusinessException("Tenes reservas activas!!! WTF ESTAS LOCO?! No podes cambiar tu tipo. Respetá")
            }
    }
}