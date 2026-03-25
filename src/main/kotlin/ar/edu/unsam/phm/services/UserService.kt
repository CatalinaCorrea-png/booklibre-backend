package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.UserTypes
import ar.edu.unsam.phm.dto.UpdateUserProfileDTO
import ar.edu.unsam.phm.dto.UserDTO
import ar.edu.unsam.phm.dto.toUserDTO
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.ConflictException
import org.springframework.web.multipart.MultipartFile


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
        if (existingUser.isEmpty()) {
            user.meetsCreationCriteria()
            userRepository.create(user)
        }  else {
            throw ConflictException("Email '${user.email}' ya se encuentra registrado")
    }}

    fun getUserById(id: Int): User =
        userRepository.getObject(id) ?: throw NotFoundException("Can not find the book <$id>")


    fun getUserProfile(userId: Int): UserDTO {
        val user = userRepository.repositoryObjects().find { user -> user.id == userId }
        if (user == null) {
            throw NotFoundException("No se encontro un user con el id: $userId")
        }
        return user.toUserDTO()
    }

    private fun saveImage(image: MultipartFile): String {
        val uploadDirectory: Path = Paths.get("uploads")

        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory)
        }

        val originalFilename = image.originalFilename ?: "image"
        val extension = originalFilename.substringAfterLast(".", "")
        val uniqueFilename = if (extension.isNotBlank()) {
            "${UUID.randomUUID()}.$extension"
        } else {
            UUID.randomUUID().toString()
        }

        val targetPath = uploadDirectory.resolve(uniqueFilename)
        Files.copy(image.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

        return "uploads/$uniqueFilename"
    }

    fun updateUserProfile(userData: UpdateUserProfileDTO, image: MultipartFile?): UserDTO {
        val existingUser = userRepository.getObject(userData.id)

        val finalImagePath = if (image != null && !image.isEmpty) {
           saveImage(image)
        } else {
            existingUser.img
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
            img = finalImagePath
        ).apply {
            id = existingUser.id
        }

        userRepository.update(updatedUser)

        return updatedUser.toUserDTO()
    }

}