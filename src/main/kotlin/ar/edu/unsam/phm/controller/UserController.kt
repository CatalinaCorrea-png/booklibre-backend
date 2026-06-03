package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.AuthenticationService
import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.*

@RestController
class UserController(private val userService: UserService, private val authenticationService: AuthenticationService) {

    @GetMapping("/profile/{userId}")
    fun getUserProfile(@PathVariable userId: String): UserDTO =
        userService.getUserProfile(userId).toUserDTO()

    @PutMapping("/updateProfile")
    fun updateUserProfile(
        @RequestPart("userData") userData: UpdateUserProfileDTO,
    ): UpdateProfileResponse {
        val updatedUser = userService.updateUserProfile(userData)
        val newToken = authenticationService.generateTokenForUser(updatedUser.email)
        return UpdateProfileResponse(updatedUser.toUserDTO(), newToken)
    }
}