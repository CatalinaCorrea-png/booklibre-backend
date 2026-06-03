package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.State
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.dto.UpdateUserProfileDTO
import ar.edu.unsam.phm.dto.toOwnerDTO
import ar.edu.unsam.phm.dto.toUserDTO
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.CrudReservationRepository
import ar.edu.unsam.phm.repository.CrudUserRepository
import ar.edu.unsam.phm.repository.MongoBookRepository
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
    private val encoder: PasswordEncoder,
    @Autowired
    val mongoBookRepository: MongoBookRepository
) {

    @Transactional(readOnly = true)
    fun getUserProfile(userId: String): User {
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

        val existingBooks = mongoBookRepository
            .findAllByOwnerId(ownerId = userData.id)

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
        val updatedUserDTO = updatedUser.toOwnerDTO()

        existingBooks.forEach { book ->
            book.owner = updatedUserDTO
            mongoBookRepository.save(book)
        }

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

    fun validateActiveReservationsAsPublisher(userId: String) {
        val reservations = reservationRepository.findByOwnerId(ownerId = userId)
        if(reservations.any { it.dropOffDate >= LocalDate.now() }) {
            throw BusinessException("Tus libros tienen reservas activas. No podés cambiar tu tipo hasta que finalicen todas las reservas de tus libros.")
        }
    }

    fun validateActiveReservationsAsReader(userId: String) {
            val reservations = reservationRepository.findAllByUser_Id(userId)
            if(reservations.any { it.dropOffDate >= LocalDate.now() }) {
                throw BusinessException("Tenés reservas activas en curso. No podés cambiar tu tipo hasta que finalicen todas tus reservas.")
            }
    }
}