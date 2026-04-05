package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.User
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

//    private fun saveImage(image: MultipartFile): String {
//        val uploadDirectory: Path = Paths.get("uploads")
//
//        if (!Files.exists(uploadDirectory)) {
//            Files.createDirectories(uploadDirectory)
//        }
//
//        val originalFilename = image.originalFilename ?: "image"
//        val extension = originalFilename.substringAfterLast(".", "")
//        val uniqueFilename = if (extension.isNotBlank()) {
//            "${UUID.randomUUID()}.$extension"
//        } else {
//            UUID.randomUUID().toString()
//        }
//
//        val targetPath = uploadDirectory.resolve(uniqueFilename)
//        Files.copy(image.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
//
//        return "uploads/$uniqueFilename"
//    }
//
//    fun updateUserProfile(userData: UpdateUserProfileDTO, image: MultipartFile?): UserDTO {
//        val existingUser = userRepository.getObject(userData.id)
//
//        val finalImagePath = if (image != null && !image.isEmpty) {
//           saveImage(image)
//        } else {
//            existingUser.img
//        }
//
//        val updatedUser = User(
//            name = userData.name,
//            description = userData.description,
//            email = userData.email,
//            cel = userData.cel,
//            location = userData.location,
//            userType = UserTypes.fromValue(userData.userType),
//            timestamp = userData.timestamp,
//            bibliokarmas = userData.bibliokarmas,
//            password = existingUser.password,
//            img = finalImagePath
//        ).apply {
//            id = existingUser.id
//        }
//
//        userRepository.update(updatedUser)
//
//        return updatedUser.toUserDTO()
//    }

}