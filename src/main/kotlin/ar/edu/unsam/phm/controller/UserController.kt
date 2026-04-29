package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.AuthenticationService
import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.*

@RestController
class UserController(private val userService: UserService, private val authenticationService: AuthenticationService) {

    @PostMapping("/register")
    fun createUser(@RequestBody request: AuthRegisterRequest): AuthResponse {
        val user = User(
            email = request.email,
            password = request.password,
            name = request.name
        )
        val savedUser = userService.create(user)
        return AuthResponse(
            email = savedUser.email,
            name = savedUser.name,
            id = savedUser.id!!  //esto esta garantizado por JPA que no va a venir null ya que el es el encargado de crearlo
        )
    }


    @GetMapping("/profile/{userId}")
    fun getUserProfile(@PathVariable userId: Long): UserDTO =
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