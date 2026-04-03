package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.services.UserService

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthResponse
import ar.edu.unsam.phm.dto.UserDTO
import ar.edu.unsam.phm.dto.toUserDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@CrossOrigin("*")
class UserController(private val userService: UserService) {

    @PostMapping("/login")
    fun getUser(@RequestBody request: AuthRequest) : AuthResponse {
        val user = User(email = request.email, password = request.password )
        val userOK = userService.getUser(user)
        return AuthResponse(email= userOK.email, name= userOK.name, id= userOK.id!!)
    }

//    @PostMapping("/register")
//    fun createUser(@RequestBody request: AuthRegisterRequest): AuthResponse {
//        val user = User(email = request.email, password = request.password, name = request.name
//        )
//        userService.create(user)
//        return AuthResponse(
//            email = user.email,
//            name = user.name,
//            id = user.id
//        )
//    }
//

    @GetMapping("/profile/{userId}")
    fun getUserProfile(@PathVariable userId: Long): UserDTO =
        userService.getUserProfile(userId).toUserDTO()
//
//    @PutMapping("/updateProfile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
//    fun updateUserProfile(
//        @RequestPart("userData") userData: UpdateUserProfileDTO,
//        @RequestPart("image", required = false) image: MultipartFile?
//    ): UserDTO =
//        userService.updateUserProfile(userData, image)
}