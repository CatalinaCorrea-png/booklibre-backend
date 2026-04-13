package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.*
import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*")
class UserController(private val userService: UserService) {

    @PostMapping("/login")
    fun getUser(@RequestBody request: AuthRequest): AuthResponse {
        val user = User(email = request.email, password = request.password)
        val userOK = userService.getUser(user)
        return AuthResponse(email = userOK.email, name = userOK.name, id = userOK.id!!)
    }

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
    ): UserDTO =
        userService.updateUserProfile(userData).toUserDTO()
}